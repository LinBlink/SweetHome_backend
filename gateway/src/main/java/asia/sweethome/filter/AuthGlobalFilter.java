package asia.sweethome.filter;

import asia.sweethome.util.JwtVerifier;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 【网关全局认证过滤器】
 * <p>
 * 网关（gateway）是所有请求进入后端的「大门」。这个过滤器就是门口的保安：每个请求都要先过它。
 * 职责：
 * <ol>
 *   <li>白名单里的接口（登录/注册/刷新等）直接放行，不查登录态；</li>
 *   <li>其余接口必须带合法的 accessToken，验签通过后，把用户 id 写进 X-User-Id 请求头，
 *       再转发给下游服务——这样下游无需再各自解析 Token，直接读这个头就知道「是谁」；</li>
 *   <li>没带 token 或 token 无效，直接返回 401，不放进来。</li>
 * </ol>
 * GlobalFilter 表示「对所有路由生效」；Ordered 用于控制多个过滤器的先后顺序。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 8:46 PM
 */
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtVerifier jwtVerifier;

    /*WHITE_LIST_STUFF*/

    /** 一条白名单规则 = 一个 HTTP 方法 + 一个路径模式（两者都匹配才算命中） */
    private record WhiteEntry(
            HttpMethod method,
            PathPattern pattern
    ) {
    }

    // 路径模式解析器，用来把 "/v1/auth/login" 这样的字符串编译成可匹配对象
    private static final PathPatternParser PARSER = PathPatternParser.defaultInstance;

    // 免登录白名单：登录、注册、刷新令牌、凭邀请码预览家庭（加入前还没登录态）
    public static final List<WhiteEntry> WHITE_LIST = List.of(
            new WhiteEntry(
                    HttpMethod.POST,
                    PARSER.parse(
                            "/v1/auth/login"
                    )
            ),
            new WhiteEntry(
                    HttpMethod.POST,
                    PARSER.parse(
                            "/v1/auth/register"
                    )
            ),
            new WhiteEntry(
                    HttpMethod.POST,
                    PARSER.parse(
                            "/v1/auth/refresh"
                    )
            ),
            new WhiteEntry(
                    HttpMethod.GET,
                    PARSER.parse(
                            "/v1/families/lookup"
                    )
            )
    );

    /**
     * 判断当前请求是否在白名单中
     * <p>
     * 白名单的作用：某些请求不需要经过认证/拦截器，直接放行。
     * 例如：登录接口、注册接口、健康检查接口等。
     *
     * @param request 服务端HTTP请求对象，包含请求方法、路径等信息
     * @return true表示在白名单中，应该放行；false表示不在白名单中，需要拦截
     */
    private boolean isWhiteListed(ServerHttpRequest request) {

        // 1. 获取HTTP请求方法（GET、POST、PUT、DELETE等）
        //    例如：如果浏览器访问 http://localhost:8080/user/login，这里就是 GET
        HttpMethod method = request.getMethod();

        // 2. 获取请求路径（去掉应用上下文路径后的部分）
        //    例如：应用上下文路径是 /myapp，请求URL是 /myapp/user/login
        //    那么 pathWithinApplication 就是 /user/login
        //    PathContainer 是Spring框架对路径的封装，方便进行路径匹配
        PathContainer pathContainer = request.getPath().pathWithinApplication();

        // 3. 使用Stream流遍历白名单列表，检查是否有任意一条规则匹配当前请求
        //    WHITE_LIST 是一个集合，里面存储了白名单规则对象
        //    每个规则对象包含：method（请求方法）和 pattern（路径匹配模式）
        return WHITE_LIST.stream().anyMatch(
                // anyMatch 方法：只要有一个元素满足条件，就返回true
                // entry 是集合中的每一个白名单规则对象
                entry ->
                        // 条件1：请求方法必须匹配
                        // 例如：白名单规则是 POST /login，当前请求也必须是 POST
                        entry.method().equals(method)
                                &&
                                // 条件2：请求路径必须匹配
                                // pattern.matches() 会进行路径模式匹配
                                // 支持通配符：例如 /user/* 可以匹配 /user/login、/user/info 等
                                entry.pattern().matches(pathContainer)
        );

        // 总结：如果请求的方法和路径同时匹配白名单中的某一条规则，
        //       则返回true，表示在白名单中，应该放行；
        //       否则返回false，表示不在白名单中，需要拦截验证。
    }

    /**
     * 过滤主逻辑。返回类型 Mono<Void> 是响应式编程的写法（网关基于 WebFlux，非阻塞），
     * 可以简单理解为「一个将来会完成的处理动作」。
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        // 1. 白名单接口直接放行（chain.filter 表示「交给后续流程继续处理」）
        if (isWhiteListed(request)) {
            return chain.filter(exchange);
        }

        // 2. 取 token：优先从 Authorization: Bearer xxx 头里取
        String authHeader = request.getHeaders().getFirst("Authorization");

        String token;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);   // 去掉前缀 "Bearer "（7 个字符）
        } else {
            // WebSocket 握手请求（浏览器无法自定义 Header）通过 Query Parameter 传递 token
            token = request.getQueryParams().getFirst("token");
        }

        if (token == null || token.isBlank()) {
            return unauthorized(exchange, "缺少登录凭证");
        }

        // 3. 用公钥验签解析 token；失败（篡改/过期）→ 401
        Claims claims;
        try {
            claims = jwtVerifier.verify(token);
        } catch (Exception e) {
            return unauthorized(exchange, "登录已失效，请重新登录");
        }

        // 4. 必须是 access 类型（refreshToken 不能用来访问业务接口）
        String type = claims.get("type", String.class);
        if (!"access".equals(type)) {
            return unauthorized(exchange, "无效的令牌类型");
        }

        String userId = claims.getSubject();

        // 5. 关键安全动作：先「删掉」请求里可能被外部伪造的 X-User-Id，再写入验签得到的真实 userId。
        //    如果不先删，攻击者就能自己塞一个 X-User-Id 冒充别人——下游是无条件信任这个头的。
        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(headers -> headers.remove("X-User-Id"))
                .header("X-User-Id", userId)
                .build();

        // 6. 带着改写后的请求继续往下转发到目标服务
        return chain.filter(
                exchange.mutate()
                        .request(mutatedRequest)
                        .build()
        );

    }

    /**
     * 直接返回 401 未授权响应，手写一段和 Result 结构一致的 JSON，让前端能统一解析。
     * （网关层没有 Controller，无法走 GlobalExceptionHandler，所以在这里手动拼 JSON。）
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("WWW-Authenticate", "Bearer");
        response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        String body = "{\"code\":401,\"message\":\"" + msg + "\",\"data\":null}";
        org.springframework.core.io.buffer.DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 过滤器执行顺序，数字越小越先执行。取 -100（较小）保证认证在大多数过滤器之前完成，
     * 未认证的请求尽早被拦下。
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
