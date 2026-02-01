package asia.sweethome.chat.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 发布端：把"某会话有新消息"/"用户上下线"广播到 Redis，供所有 chat-service 实例的订阅者转发给
 * 本实例持有的 WebSocket 连接。Payload 只携带 id，接收方按需重新查询数据库/组装个性化视图
 * （比如 senderRelationLabel 是相对每个接收者单独计算的，没法在广播时预先烤入一份通用内容）。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageRelay {

    public static final String PRESENCE_CHANNEL = "presence";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishNewMessage(Long conversationId, Long messageId) {
        publish(conversationChannel(conversationId), Map.of(
                "kind", "MESSAGE",
                "conversationId", conversationId,
                "messageId", messageId
        ));
    }

    public void publishPresence(Long userId, boolean online) {
        publish(PRESENCE_CHANNEL, Map.of(
                "kind", "PRESENCE",
                "userId", userId,
                "status", online ? "online" : "offline"
        ));
    }

    public static String conversationChannel(Long conversationId) {
        return "conv:" + conversationId;
    }

    private void publish(String channel, Map<String, Object> payload) {
        try {
            redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.error("Redis publish 失败, channel={}", channel, e);
        }
    }
}
