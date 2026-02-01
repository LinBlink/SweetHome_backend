package asia.sweethome.chat.ws;

import asia.sweethome.chat.entity.po.ConversationMember;
import asia.sweethome.chat.entity.po.Message;
import asia.sweethome.chat.entity.vo.MessageVO;
import asia.sweethome.chat.service.ChatAssembler;
import asia.sweethome.chat.service.IConversationMembersService;
import asia.sweethome.chat.service.IMessagesService;
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
 * 订阅端：每个 chat-service 实例都会收到全部广播，只把与"本实例本地持有的连接"相关的部分转发出去。
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

    @Override
    public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            JsonNode payload = objectMapper.readTree(message.getBody());
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

    private void handleNewMessage(JsonNode payload) {
        Long conversationId = payload.path("conversationId").asLong();
        Long messageId = payload.path("messageId").asLong();

        List<ConversationMember> activeMembers = conversationMembersService.listActiveMembers(conversationId);
        Set<Long> localUserIds = localSessionRegistry.localUserIds();

        List<ConversationMember> localRecipients = activeMembers.stream()
                .filter(m -> localUserIds.contains(m.getUserId()))
                .toList();

        if (localRecipients.isEmpty()) {
            return;
        }

        Message dbMessage = messagesService.getById(messageId);
        if (dbMessage == null) {
            return;
        }

        for (ConversationMember recipient : localRecipients) {
            for (WebSocketSession session : localSessionRegistry.sessionsOf(recipient.getUserId())) {
                String lang = (String) session.getAttributes().get(WsSessionAttributes.ACCEPT_LANGUAGE);
                MessageVO vo = chatAssembler.toMessageVO(dbMessage, recipient.getUserId(), lang);
                send(session, Map.of("type", "NEW_MESSAGE", "data", vo));
            }
        }
    }

    private void handlePresence(JsonNode payload) {
        Long userId = payload.path("userId").asLong();
        String status = payload.path("status").asText();

        List<Long> conversationIds = conversationMembersService.listActiveConversationIds(userId);
        if (conversationIds.isEmpty()) {
            return;
        }

        Set<Long> localUserIds = localSessionRegistry.localUserIds();
        Set<Long> notified = new java.util.HashSet<>();

        for (Long conversationId : conversationIds) {
            for (ConversationMember member : conversationMembersService.listActiveMembers(conversationId)) {
                Long memberUserId = member.getUserId();
                if (memberUserId.equals(userId) || !localUserIds.contains(memberUserId) || !notified.add(memberUserId)) {
                    continue;
                }
                for (WebSocketSession session : localSessionRegistry.sessionsOf(memberUserId)) {
                    send(session, Map.of("type", "USER_STATUS", "data", Map.of("userId", userId, "status", status)));
                }
            }
        }
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
