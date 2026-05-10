package asia.sweethome.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 【Spring Security 安全配置】
 * <p>
 * 引入 Spring Security 后，它默认会「拦截所有请求要求登录」。auth-service 自己就是登录服务、
 * 又躲在网关后面，所以这里把默认那套表单登录关掉，只借用它的两样东西：
 * <ol>
 *   <li>{@link BCryptPasswordEncoder} —— 密码加密器；</li>
 *   <li>放行 /v1/auth/** —— 让注册/登录等接口无需登录即可访问。</li>
 * </ol>
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 7:46 PM
 */
@Configuration
public class SecurityConfig {

    /**
     * 密码加密器。BCrypt 是专门为存密码设计的哈希算法：
     * <ul>
     *   <li>不可逆——只能验证不能反解，即便数据库泄漏也拿不到原始密码；</li>
     *   <li>自带随机盐——同样的密码每次加密结果都不同，防止「彩虹表」批量破解；</li>
     *   <li>故意算得慢——增加暴力破解成本。</li>
     * </ul>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 定制安全过滤链。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF：这是给「浏览器表单 + Cookie」场景防跨站请求伪造用的，
                // 我们是无状态的 Token 认证（不靠 Cookie），用不到，关掉以免拦截接口
                .csrf(csrf -> csrf.disable())
                // 放行认证相关接口：注册/登录本就该匿名访问
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers("/v1/auth/**").permitAll()
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/doc.html"
                                )
                                .permitAll()
                )
                // 关闭 Spring Security 自带的登录页表单和浏览器弹窗式 Basic 认证
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

}
