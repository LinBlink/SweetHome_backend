package asia.sweethome.chat.ws;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 【Redis 消息广播 · 发布端】
 * <p>
 * 为什么需要它？聊天服务会部署多个实例（chat-service-1、chat-service-2……），用户 A 的连接
 * 可能在实例 1，他要发给的用户 B 的连接却在实例 2。单靠某个实例内存里的连接表推不到别的实例。
 * 于是引入 Redis 的「发布/订阅」：谁有新消息，就往 Redis 一个频道里喊一嗓子（publish），
 * 所有实例都订阅这个频道、都能听到，再各自把消息推给「自己这台机器上」的目标连接。
 * <p>
 * 广播内容只带 id（会话 id / 消息 id），不带完整消息体。因为每个接收者看到的称谓
 * （senderRelationLabel）是因人而异的，需要接收端按 id 重新查库、按接收者视角组装。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageRelay {

    public static final String PRESENCE_CHANNEL = "presence";   // 上下线事件的频道名

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** 广播「某会话有新消息」到该会话专属频道（conv:会话id） */
    public void publishNewMessage(Long conversationId, Long messageId) {
        publish(conversationChannel(conversationId), Map.of(
                "kind", "MESSAGE",
                "conversationId", conversationId,
                "messageId", messageId
        ));
    }

    /** 广播「某用户上线/下线」到 presence 频道 */
    public void publishPresence(Long userId, boolean online) {
        publish(PRESENCE_CHANNEL, Map.of(
                "kind", "PRESENCE",
                "userId", userId,
                "status", online ? "online" : "offline"
        ));
    }

    /** 每个会话有独立频道名，订阅方按 conv:* 模式订阅所有会话频道 */
    public static String conversationChannel(Long conversationId) {
        return "conv:" + conversationId;
    }

    /** 把 payload 序列化成 JSON 发到指定频道。广播失败只记录日志，不抛错影响主流程 */
    private void publish(String channel, Map<String, Object> payload) {
        try {
            redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.error("Redis publish 失败, channel={}", channel, e);
        }
    }
}
