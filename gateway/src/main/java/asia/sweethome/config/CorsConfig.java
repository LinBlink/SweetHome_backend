package asia.sweethome.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 【CORS 跨域配置】
 * <p>
 * CORS（跨域资源共享）解决的问题：浏览器出于安全，默认禁止「网页所在域名」去请求「另一个域名」的接口。
 * 前端页面和后端网关往往不同源，所以需要在网关这里显式声明「允许哪些来源来调我」。
 * <p>
 * ⚠️ 当前是【开发期配置】：allowedOriginPattern 设成了 "*"（放行所有来源），仅供本地联调方便。
 * 目标生产策略（见 doc/api.md 「六、微服务架构总览」）是只放行本地开发源与 sweethome.example.com
 * 子域。上线前必须按下面 TODO 收紧——尤其在 allowCredentials(true)（允许携带 Cookie/凭证）的情况下，
 * 放通所有来源存在安全风险。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 10:32 下午
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // TODO 上线前改为具体白名单，如：
        //   config.addAllowedOriginPattern("http://localhost:*");
        //   config.addAllowedOriginPattern("https://*.sweethome.example.com");
        config.addAllowedOriginPattern("*");   // 允许的来源（* = 全部，仅开发用）
        // 允许的 HTTP 方法
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");   // 浏览器跨域「预检请求」用的方法
        config.addAllowedHeader("*");         // 允许所有请求头
        config.setAllowCredentials(true);     // 允许携带凭证（Cookie 等）

        // 把上面这套规则应用到所有路径 /**
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

}
