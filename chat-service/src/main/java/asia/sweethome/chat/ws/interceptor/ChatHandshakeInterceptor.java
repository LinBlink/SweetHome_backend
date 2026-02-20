package asia.sweethome.chat.ws.interceptor;

import asia.sweethome.chat.util.JwtVerifier;
import asia.sweethome.chat.ws.WsSessionAttributes;
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
 * 【WebSocket 握手拦截器】
 * <p>
 * WebSocket 连接建立前会先走一次「握手」（本质是一个 HTTP 请求）。这里在握手阶段完成鉴权，
 * 确认「你是谁」，并把用户 id 存进连接的 attributes，之后整条连接上的消息都能拿到这个身份。
 * 握手失败（认不出身份）直接拒绝，连接就建立不起来。
 * <p>
 * 身份来源优先级：
 * <ol>
 *   <li>X-User-Id 请求头（正常路径：网关 AuthGlobalFilter 已验证 JWT 并注入）；</li>
 *   <li>?token= 查询参数（脱离网关直连调试的兜底——浏览器原生 WebSocket 无法自定义请求头，
 *       只能把 token 放 URL 上，这里自行验签）。</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtVerifier jwtVerifier;

    /** 握手前置校验：解析出用户 id 则放行并记录身份，否则返回 401 拒绝连接 */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {

        // 先试请求头，取不到再退回用 URL 上的 token
        Long userId = resolveFromHeader(request);
        if (userId == null) {
            userId = resolveFromQueryToken(request);
        }

        if (userId == null) {
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;   // 认不出身份，拒绝握手
        }

        // 把身份和语言偏好存进连接属性，后续处理器通过 session.getAttributes() 读取
        attributes.put(WsSessionAttributes.USER_ID, userId);
        attributes.put(WsSessionAttributes.ACCEPT_LANGUAGE, request.getHeaders().getFirst("Accept-Language"));

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
    }

    /** 从 X-User-Id 请求头解析用户 id（网关已鉴权注入，可信任） */
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

    /** 从 URL 的 ?token= 参数解析：自行验签 JWT，且必须是 access 类型 */
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
            // 得到 userId
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            log.warn("WebSocket 握手 token 校验失败", e);
            return null;
        }
    }
}
