package asia.sweethome.chat.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 跨实例在线状态：以 Redis Set 记录当前在任意 chat-service 实例上有活跃 WebSocket 连接的 userId。
 */
@Component
@RequiredArgsConstructor
public class OnlineUserRegistry {

    private static final String KEY = "chat:online-users";

    private final StringRedisTemplate redisTemplate;

    public void markOnline(Long userId) {
        redisTemplate.opsForSet().add(KEY, String.valueOf(userId));
    }

    public void markOffline(Long userId) {
        redisTemplate.opsForSet().remove(KEY, String.valueOf(userId));
    }

    public boolean isOnline(Long userId) {
        Boolean member = redisTemplate.opsForSet().isMember(KEY, String.valueOf(userId));
        return Boolean.TRUE.equals(member);
    }

    public List<Long> filterOnline(List<Long> userIds) {
        return userIds.stream().filter(this::isOnline).toList();
    }
}
