package asia.sweethome.chat.ws.handler;

import asia.sweethome.chat.entity.po.Message;
import asia.sweethome.chat.service.IConversationMembersService;
import asia.sweethome.chat.service.IMessagesService;
import asia.sweethome.chat.ws.*;
import asia.sweethome.chat.ws.registry.LocalSessionRegistry;
import asia.sweethome.chat.ws.registry.OnlineUserRegistry;
import asia.sweethome.chat.ws.registry.RedisMessageRelay;
import asia.sweethome.common.constants.MessageTypeConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * 【WebSocket 消息处理器】原始 WebSocket（非 STOMP）端点，帧格式见 doc/api.md 「五、WebSocket 接口」。
 * <p>
 * WebSocket 是浏览器与服务器之间的「长连接」，建立一次后双方可随时互发消息，特别适合聊天这类
 * 需要「服务器主动推」的场景（HTTP 只能客户端问一句、服务器答一句）。
 * 本类继承 TextWebSocketHandler，覆写三个生命周期回调：
 * <ul>
 *   <li>afterConnectionEstablished：连接建立（用户上线）；</li>
 *   <li>handleTextMessage：收到客户端发来的一帧文本；</li>
 *   <li>afterConnectionClosed：连接断开（用户可能下线）。</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final int MAX_MESSAGE_LENGTH = 2000;  // 单条消息最大字符数

    private final LocalSessionRegistry localSessionRegistry;   // 本实例内存里的连接表
    private final OnlineUserRegistry onlineUserRegistry;       // Redis 里的全局在线名单
    private final RedisMessageRelay redisMessageRelay;         // 往 Redis 广播
    private final IConversationMembersService conversationMembersService;
    private final IMessagesService messagesService;
    private final ObjectMapper objectMapper;                   // JSON 与对象互转

    /** 连接建立：把这条连接登记到本机，并标记该用户在线、广播上线事件 */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = userId(session);
        localSessionRegistry.register(userId, session); // 连接登录本机
        onlineUserRegistry.markOnline(userId); // 标记用户在线
        redisMessageRelay.publishPresence(userId, true); // 广播上线事件
    }

    /**
     * 连接关闭：从本机连接表移除。只有当该用户在本机「最后一条」连接也断了，
     * 才真正标记离线并广播——因为同一用户可能开了多个设备/标签页。
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = userId(session);
        boolean lastLocalSession = localSessionRegistry.unregister(userId, session);
        if (lastLocalSession) {
            onlineUserRegistry.markOffline(userId);
            redisMessageRelay.publishPresence(userId, false);
        }
    }

    /**
     * 收到客户端发来的一帧文本。先把 JSON 解析成 InboundFrame，再按 type 分派处理。
     * 客户端约定几种 type：PING（心跳）、SEND_MESSAGE（发消息）、READ（上报已读）、JOIN_CONVERSATION。
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = userId(session);

        // 解析 JSON 帧，解析失败说明格式不对，回一个错误帧
        InboundFrame frame;
        try {
            frame = objectMapper.readValue(message.getPayload(), InboundFrame.class); // message -> JSON
        } catch (Exception e) {
            sendError(session, "PARAM_ERROR", "无法解析的消息帧");
            return;
        }

        if (frame.getType() == null) {
            sendError(session, "PARAM_ERROR", "缺少 type 字段");
            return;
        }

        switch (frame.getType()) {
            case "PING" -> send(session, Map.of("type", "PONG"));   // 心跳：回 PONG 保活
            case "SEND_MESSAGE" -> handleSendMessage(session, userId, frame);
            case "READ" -> handleRead(userId, frame);
            case "JOIN_CONVERSATION" -> {
                // 预留：当前实现无需服务端额外状态即可完成消息推送，此帧仅作为客户端信令被接受
            }
            default -> sendError(session, "PARAM_ERROR", "未知的帧类型: " + frame.getType());
        }
    }

    /**
     * 处理「发消息」：校验 → 落库 → 广播。
     * 注意：这里发消息人自己并不直接把消息回显，而是统一走「落库 + Redis 广播 + 订阅者推送」
     * 这条路径，保证多设备、多实例看到的消息完全一致（自己的其它设备也会收到）。
     */
    private void handleSendMessage(WebSocketSession session, Long userId, InboundFrame frame) {
        if (frame.getConversationId() == null) {
            sendError(session, "PARAM_ERROR", "缺少 conversationId");
            return;
        }
        // 鉴权：必须是该会话的活跃成员才能发言，防止越权往别人会话里发消息
        if (!conversationMembersService.isActiveMember(frame.getConversationId(), userId)) {
            sendError(session, "UNAUTHORIZED", "无权访问该会话");
            return;
        }
        if (frame.getContent() != null && frame.getContent().length() > MAX_MESSAGE_LENGTH) {
            sendError(session, "MESSAGE_TOO_LONG", "消息内容超长");
            return;
        }

        String dbType = toDbMessageType(frame.getMessageType());

        // 落库（内部按 clientId 去重，重复投递不会存两条）
        Message saved = messagesService.send(
                frame.getConversationId(), userId, dbType, frame.getContent(), frame.getClientId(), null
        );

        // 广播「这个会话有新消息 id=xxx」，各实例的订阅者据此推给本地在线的成员
        redisMessageRelay.publishNewMessage(frame.getConversationId(), saved.getId());
    }

    /** 处理「已读上报」：把该用户在此会话的已读进度更新到指定消息 id */
    private void handleRead(Long userId, InboundFrame frame) {
        if (frame.getConversationId() == null || frame.getLastReadMessageId() == null) {
            return;
        }
        conversationMembersService.markRead(frame.getConversationId(), userId, frame.getLastReadMessageId());
    }

    /** 把客户端传的类型（大小写不敏感）归一成数据库存的小写枚举值，非法值一律当作文本 */
    private String toDbMessageType(String messageType) {
        if (messageType == null) {
            return MessageTypeConstants.TEXT;
        }
        return switch (messageType.toUpperCase()) {
            case "IMAGE" -> MessageTypeConstants.IMAGE;
            case "VOICE" -> MessageTypeConstants.VOICE;
            case "SYSTEM" -> MessageTypeConstants.SYSTEM;
            default -> MessageTypeConstants.TEXT;
        };
    }

    /** 从连接的附加属性里取出握手阶段存好的用户 id（见 ChatHandshakeInterceptor） */
    private Long userId(WebSocketSession session) {
        return (Long) session.getAttributes().get(WsSessionAttributes.USER_ID);
    }

    /** 给客户端回一个统一格式的错误帧 */
    private void sendError(WebSocketSession session, String code, String message) {
        send(session, Map.of("type", "ERROR", "data", Map.of("code", code, "message", message)));
    }

    /** 把任意对象转成 JSON 通过连接发出去。连接已关或发送异常都安全跳过，不影响主流程 */
    private void send(WebSocketSession session, Object payload) {
        if (!session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (Exception e) {
            log.warn("WebSocket 推送失败, sessionId={}", session.getId(), e);
        }
    }
}
