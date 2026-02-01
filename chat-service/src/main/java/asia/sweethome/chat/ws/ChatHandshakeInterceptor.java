package asia.sweethome.chat.ws;

import asia.sweethome.chat.util.JwtVerifier;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * 身份来源优先级：
 *  1. X-User-Id 请求头（正常路径，网关 AuthGlobalFilter 已验证 JWT 并注入）
 *  2. ?token= 查询参数（脱离网关直连 chat-service 调试时的兜底，浏览器 WebSocket API 无法自定义 Header）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtVerifier jwtVerifier;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {

        Long userId = resolveFromHeader(request);
        if (userId == null) {
            userId = resolveFromQueryToken(request);
        }

        if (userId == null) {
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(WsSessionAttributes.USER_ID, userId);
        attributes.put(WsSessionAttributes.ACCEPT_LANGUAGE, request.getHeaders().getFirst("Accept-Language"));

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
    }

    private Long resolveFromHeader(ServerHttpRequest request) {
        String userIdHeader = request.getHeaders().getFirst("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(userIdHeader);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long resolveFromQueryToken(ServerHttpRequest request) {
        List<String> tokenParams = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .get("token");

        if (tokenParams == null || tokenParams.isEmpty()) {
            return null;
        }

        try {
            Claims claims = jwtVerifier.verify(tokenParams.get(0));
            if (!"access".equals(claims.get("type", String.class))) {
                return null;
            }
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            log.warn("WebSocket 握手 token 校验失败", e);
            return null;
        }
    }
}
