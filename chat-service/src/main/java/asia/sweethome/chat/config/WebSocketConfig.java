package asia.sweethome.chat.config;

import asia.sweethome.chat.ws.ChatHandshakeInterceptor;
import asia.sweethome.chat.ws.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 原始 WebSocket 端点 /v1/ws（网关侧路径为 /api/v1/ws，经 RewritePath 去掉 /api 前缀后落到这里）。
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ChatHandshakeInterceptor chatHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/v1/ws")
                .addInterceptors(chatHandshakeInterceptor)
                .setAllowedOriginPatterns("http://localhost:*", "https://*.sweethome.example.com");
    }
}
