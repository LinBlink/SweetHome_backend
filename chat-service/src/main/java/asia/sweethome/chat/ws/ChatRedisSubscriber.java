package asia.sweethome.chat.ws;

import asia.sweethome.chat.entity.po.ConversationMember;
import asia.sweethome.chat.entity.po.Message;
import asia.sweethome.chat.entity.vo.MessageVO;
import asia.sweethome.chat.service.ChatAssembler;
import asia.sweethome.chat.service.IConversationMembersService;
import asia.sweethome.chat.service.IMessagesService;
import asia.sweethome.chat.ws.registry.LocalSessionRegistry;
import asia.sweethome.chat.ws.registry.RedisMessageRelay;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 【Redis 消息广播 · 订阅端】
 * <p>
 * 与 {@link RedisMessageRelay}（发布端）配对。每个 chat-service 实例都订阅了全部广播，
 * 但一条广播来了，本实例只负责把它推给「正好连在本机上」的目标用户，其它用户由别的实例负责。
 * 这样多实例协同，消息才能可靠地送达到任意一台机器上的任意用户。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatRedisSubscriber implements MessageListener {

    private final LocalSessionRegistry localSessionRegistry;
    private final IConversationMembersService conversationMembersService;
    private final IMessagesService messagesService;
    private final ChatAssembler chatAssembler;
    private final ObjectMapper objectMapper;

    /** Redis 收到广播时的回调入口，按 kind 分派：新消息 or 上下线 */
    @Override
    public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            JsonNode payload = objectMapper.readTree(message.getBody());  // 解析 JSON
            String kind = payload.path("kind").asText();

            if ("MESSAGE".equals(kind)) {
                handleNewMessage(payload);
            } else if ("PRESENCE".equals(kind)) {
                handlePresence(payload);
            }
        } catch (Exception e) {
            log.error("处理 Redis Pub/Sub 消息失败", e);
        }
    }

    /** 处理「新消息」广播：找出本机上属于该会话的接收者，按各自视角组装后推送 */
    private void handleNewMessage(JsonNode payload) {
        Long conversationId = payload.path("conversationId").asLong();
        Long messageId = payload.path("messageId").asLong();

        // 该会话全部活跃成员 ∩ 本机在线用户 = 本机需要推送的接收者
        List<ConversationMember> activeMembers = conversationMembersService.listActiveMembers(conversationId);
        Set<Long> localUserIds = localSessionRegistry.localUserIds();

        List<ConversationMember> localRecipients = activeMembers.stream()
                .filter(m -> localUserIds.contains(m.getUserId()))
                .toList();

        if (localRecipients.isEmpty()) {
            return;   // 本机没人要收这条，直接返回（交给别的实例处理）
        }

        // 按 id 重新查完整消息
        Message dbMessage = messagesService.getById(messageId);
        if (dbMessage == null) {
            return;
        }

        // 逐个接收者、逐条连接推送。称谓因人而异，故每个接收者单独组装 VO
        for (ConversationMember recipient : localRecipients) {
            for (WebSocketSession session : localSessionRegistry.sessionsOf(recipient.getUserId())) {
                String lang = (String) session.getAttributes().get(WsSessionAttributes.ACCEPT_LANGUAGE);
                MessageVO vo = chatAssembler.toMessageVO(dbMessage, recipient.getUserId(), lang);
                send(session, Map.of("type", "NEW_MESSAGE", "data", vo));
            }
        }
    }

    /**
     * 处理「上下线」广播：通知「和上下线者有共同会话」且「连在本机」的其他人，
     * 好让他们的界面实时更新联系人在线状态。
     */
    private void handlePresence(JsonNode payload) {
        Long userId = payload.path("userId").asLong();     // 上下线的人
        String status = payload.path("status").asText();   // online / offline

        // 该用户参与的所有会话
        List<Long> conversationIds = conversationMembersService.listActiveConversationIds(userId);
        if (conversationIds.isEmpty()) {
            return;
        }

        Set<Long> localUserIds = localSessionRegistry.localUserIds();
        Set<Long> notified = new java.util.HashSet<>();   // 去重：同一人可能在多个共同会话里，只通知一次

        for (Long conversationId : conversationIds) {
            for (ConversationMember member : conversationMembersService.listActiveMembers(conversationId)) {
                Long memberUserId = member.getUserId();
                // 跳过：本人、不在本机的人、已通知过的人（notified.add 返回 false 表示已存在）
                if (memberUserId.equals(userId) || !localUserIds.contains(memberUserId) || !notified.add(memberUserId)) {
                    continue;
                }
                for (WebSocketSession session : localSessionRegistry.sessionsOf(memberUserId)) {
                    send(session, Map.of("type", "USER_STATUS", "data", Map.of("userId", userId, "status", status)));
                }
            }
        }
    }

    /** 把对象转 JSON 通过连接推给客户端；连接已关或异常都安全跳过 */
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
