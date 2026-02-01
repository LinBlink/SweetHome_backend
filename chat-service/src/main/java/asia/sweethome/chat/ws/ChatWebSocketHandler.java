package asia.sweethome.chat.ws;

import asia.sweethome.chat.entity.po.Message;
import asia.sweethome.chat.service.IConversationMembersService;
import asia.sweethome.chat.service.IMessagesService;
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
 * 原始 WebSocket（非 STOMP）端点，帧格式见 doc/api.md 「五、WebSocket 接口」。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final LocalSessionRegistry localSessionRegistry;
    private final OnlineUserRegistry onlineUserRegistry;
    private final RedisMessageRelay redisMessageRelay;
    private final IConversationMembersService conversationMembersService;
    private final IMessagesService messagesService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = userId(session);
        localSessionRegistry.register(userId, session);
        onlineUserRegistry.markOnline(userId);
        redisMessageRelay.publishPresence(userId, true);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = userId(session);
        boolean lastLocalSession = localSessionRegistry.unregister(userId, session);
        if (lastLocalSession) {
            onlineUserRegistry.markOffline(userId);
            redisMessageRelay.publishPresence(userId, false);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = userId(session);

        InboundFrame frame;
        try {
            frame = objectMapper.readValue(message.getPayload(), InboundFrame.class);
        } catch (Exception e) {
            sendError(session, "PARAM_ERROR", "无法解析的消息帧");
            return;
        }

        if (frame.getType() == null) {
            sendError(session, "PARAM_ERROR", "缺少 type 字段");
            return;
        }

        switch (frame.getType()) {
            case "PING" -> send(session, Map.of("type", "PONG"));
            case "SEND_MESSAGE" -> handleSendMessage(session, userId, frame);
            case "READ" -> handleRead(userId, frame);
            case "JOIN_CONVERSATION" -> {
                // 预留：当前实现无需服务端额外状态即可完成消息推送，此帧仅作为客户端信令被接受
            }
            default -> sendError(session, "PARAM_ERROR", "未知的帧类型: " + frame.getType());
        }
    }

    private void handleSendMessage(WebSocketSession session, Long userId, InboundFrame frame) {
        if (frame.getConversationId() == null) {
            sendError(session, "PARAM_ERROR", "缺少 conversationId");
            return;
        }
        if (!conversationMembersService.isActiveMember(frame.getConversationId(), userId)) {
            sendError(session, "UNAUTHORIZED", "无权访问该会话");
            return;
        }
        if (frame.getContent() != null && frame.getContent().length() > MAX_MESSAGE_LENGTH) {
            sendError(session, "MESSAGE_TOO_LONG", "消息内容超长");
            return;
        }

        String dbType = toDbMessageType(frame.getMessageType());

        Message saved = messagesService.send(
                frame.getConversationId(), userId, dbType, frame.getContent(), frame.getClientId(), null
        );

        redisMessageRelay.publishNewMessage(frame.getConversationId(), saved.getId());
    }

    private void handleRead(Long userId, InboundFrame frame) {
        if (frame.getConversationId() == null || frame.getLastReadMessageId() == null) {
            return;
        }
        conversationMembersService.markRead(frame.getConversationId(), userId, frame.getLastReadMessageId());
    }

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

    private Long userId(WebSocketSession session) {
        return (Long) session.getAttributes().get(WsSessionAttributes.USER_ID);
    }

    private void sendError(WebSocketSession session, String code, String message) {
        send(session, Map.of("type", "ERROR", "data", Map.of("code", code, "message", message)));
    }

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
