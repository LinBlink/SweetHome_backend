package asia.sweethome.chat.ws.config;

import asia.sweethome.chat.ws.interceptor.ChatHandshakeInterceptor;
import asia.sweethome.chat.ws.handler.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 【WebSocket 配置】
 * <p>
 * 把处理器和握手拦截器「挂」到 /v1/ws 这个 WebSocket 端点上
 * （网关侧路径为 /api/v1/ws，经 RewritePath 去掉 /api 前缀后落到这里）。
 */
@Configuration
@EnableWebSocket   // 开启 WebSocket 支持
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;         // 处理消息收发
    private final ChatHandshakeInterceptor chatHandshakeInterceptor;// 握手时鉴权

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/v1/ws")
                .addInterceptors(chatHandshakeInterceptor)
                // 允许跨域来源（本地开发端口 + 生产域名），防止任意网站连我们的 WebSocket
                // TODO 正式上线时更改
                .setAllowedOriginPatterns("http://localhost:*","http://127.0.0.1:*", "https://*.sweethome.example.com");
    }
}
