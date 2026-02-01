package asia.sweethome.chat.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 单个 chat-service 实例内存中持有的 WebSocket 连接表：userId -&gt; 该用户在本实例上的所有会话
 * （同一用户可能有多个设备/标签页同时在线）。多实例之间靠 Redis Pub/Sub 广播打通。
 */
@Component
public class LocalSessionRegistry {

    private final Map<Long, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        sessionsByUserId.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    /**
     * 移除该会话；返回移除后该用户在本实例上是否已无任何在线连接。
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

    public Set<WebSocketSession> sessionsOf(Long userId) {
        return sessionsByUserId.getOrDefault(userId, Collections.emptySet());
    }

    public Set<Long> localUserIds() {
        return sessionsByUserId.keySet();
    }
}
