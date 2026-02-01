package asia.sweethome.chat.config;

import asia.sweethome.chat.ws.ChatRedisSubscriber;
import asia.sweethome.chat.ws.RedisMessageRelay;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ChatRedisSubscriber chatRedisSubscriber;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(chatRedisSubscriber, new PatternTopic("conv:*"));
        container.addMessageListener(chatRedisSubscriber, new PatternTopic(RedisMessageRelay.PRESENCE_CHANNEL));
        return container;
    }
}
