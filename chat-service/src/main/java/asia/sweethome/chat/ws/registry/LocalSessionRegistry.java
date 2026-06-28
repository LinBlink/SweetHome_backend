package asia.sweethome.chat.ws.registry;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import asia.sweethome.chat.ws.RedisMessageRelay;

/**
 * 【本机 WebSocket 连接表】
 * <p>
 * 记录「本实例内存里」当前持有的连接：userId -&gt; 该用户在本机的所有连接
 * （同一用户可能开了多个设备/标签页，故一个 userId 对应一个连接集合）。
 * <p>
 * 用到的并发容器都是线程安全的（多个连接可能同时读写这张表）：
 * ConcurrentHashMap（并发安全的 Map）+ CopyOnWriteArraySet（读多写少场景下并发安全的 Set）。
 * 跨实例的协同由 Redis Pub/Sub 负责，见 {@link RedisMessageRelay}。
 */
@Component
public class LocalSessionRegistry {

    private final Map<Long, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();

    /** 登记一条新连接。computeIfAbsent：该用户还没有集合就先建一个，再把连接加进去 */
    public void register(Long userId, WebSocketSession session) {
        sessionsByUserId.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    /**
     * 移除该连接；返回 true 表示移除后该用户在本机已无任何连接（即真正下线，需触发离线广播）。
     */
    public boolean unregister(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null) {
            return true;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByUserId.remove(userId);
            return true;
        }
        return false;
    }

    /** 取某用户在本机的所有连接（没有则返回空集合） */
    public Set<WebSocketSession> sessionsOf(Long userId) {
        return sessionsByUserId.getOrDefault(userId, Collections.emptySet());
    }

    /** 本机当前在线的全部用户 id */
    public Set<Long> localUserIds() {
        return sessionsByUserId.keySet();
    }
}
