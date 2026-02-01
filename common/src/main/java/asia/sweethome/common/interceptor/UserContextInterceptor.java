package asia.sweethome.common.interceptor;

import asia.sweethome.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 【用户上下文拦截器】
 * <p>
 * 拦截器（Interceptor）是 Spring MVC 提供的「关卡」：在 Controller 处理请求「之前」和「之后」
 * 各插入一段逻辑。这个拦截器负责给每个请求「上下车」当前登录用户：
 * <ul>
 *   <li>{@link #preHandle} —— 请求进来时，从请求头 X-User-Id 读出用户 ID，存进 {@link UserContext}；</li>
 *   <li>{@link #afterCompletion} —— 请求处理完，清空 {@link UserContext}，防止线程复用串号。</li>
 * </ul>
 * X-User-Id 由网关在校验 Token 通过后写入，业务服务信任该请求头。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 9:53 下午
 */
@Slf4j
public class UserContextInterceptor implements HandlerInterceptor {

    /**
     * 请求到达 Controller 之前执行。
     * @return true 表示放行（继续往下走）；返回 false 会直接中断请求。这里永远放行，
     *         「有没有登录、够不够权限」交给网关和具体业务去判断。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 网关转发时写入的用户 ID，未登录接口可能没有这个头
        String userIdStr = request.getHeader("X-User-Id");

        // StringUtils.hasText：判断字符串非 null、非空、且不全是空格
        if (StringUtils.hasText(userIdStr)) {
            try {
                UserContext.set(Long.valueOf(userIdStr));   // 字符串转 Long 存入上下文
            } catch (NumberFormatException e) {
                // 头部格式异常属于极端情况（正常由网关保证是数字）。
                // 这里选择「忽略并记录」而不是抛错，避免一个坏请求头把整个接口打成 500。
                log.warn("非法的 X-User-Id 请求头: {}", userIdStr);
            }
        }

        return true;
    }

    /**
     * 请求彻底处理完毕后执行（无论成功还是抛异常都会走到）。
     * 必须在这里清理 ThreadLocal，否则线程归还线程池后会污染下一个请求。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserContext.clear();
    }
}
