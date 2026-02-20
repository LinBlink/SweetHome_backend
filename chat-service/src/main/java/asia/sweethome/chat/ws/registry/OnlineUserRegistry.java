package asia.sweethome.chat.ws.registry;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 【全局在线名单】
 * <p>
 * 与 {@link LocalSessionRegistry}（只知道本机）不同，这里用一个 Redis 集合（Set）
 * 记录「在任意一台 chat-service 上有活跃连接」的所有 userId，是全局视角。
 * 别的服务（如 family-service 查成员在线状态）通过 Dubbo 最终读的就是这份数据。
 */
@Component
@RequiredArgsConstructor
public class OnlineUserRegistry {

    private static final String KEY = "chat:online-users";   // Redis 里的集合键名

    private final StringRedisTemplate redisTemplate;

    /** 标记上线：把 userId 加入集合（Set 天然去重，重复加无副作用） */
    public void markOnline(Long userId) {
        redisTemplate.opsForSet().add(KEY, String.valueOf(userId));
    }

    /** 标记下线：从集合移除 */
    public void markOffline(Long userId) {
        redisTemplate.opsForSet().remove(KEY, String.valueOf(userId));
    }

    /** 某人是否在线 = 是否在集合里 */
    public boolean isOnline(Long userId) {
        Boolean member = redisTemplate.opsForSet().isMember(KEY, String.valueOf(userId));
        return Boolean.TRUE.equals(member);   // 用 Boolean.TRUE.equals 兼顾 null，避免拆箱空指针
    }

    /** 从一批 userId 中筛出在线的 */
    public List<Long> filterOnline(List<Long> userIds) {
        return userIds.stream().filter(this::isOnline).toList();
    }
}
