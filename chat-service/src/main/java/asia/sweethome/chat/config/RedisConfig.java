package asia.sweethome.chat.config;

import asia.sweethome.chat.ws.ChatRedisSubscriber;
import asia.sweethome.chat.ws.registry.RedisMessageRelay;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 【Redis 订阅配置】
 * <p>
 * 让本实例订阅两类频道，收到广播时回调 {@link ChatRedisSubscriber}：
 * <ul>
 *   <li>conv:*  —— 所有会话消息频道（* 是通配，匹配 conv:1、conv:2……）；</li>
 *   <li>presence —— 上下线事件频道。</li>
 * </ul>
 * 这就是「多实例靠 Redis 打通」的接线处。
 */
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ChatRedisSubscriber chatRedisSubscriber;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // PatternTopic 支持通配订阅；把订阅者注册到两类频道上
        container.addMessageListener(chatRedisSubscriber, new PatternTopic("conv:*"));
        container.addMessageListener(chatRedisSubscriber, new PatternTopic(RedisMessageRelay.PRESENCE_CHANNEL));
        return container;
    }
}
