package asia.sweethome.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 【gateway 网关启动类】
 * <p>
 * 网关是整个后端的统一入口，负责鉴权、跨域、把请求路由到各个业务服务。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 7:14 PM
 */
// ⚠️ 这里显式声明 @ComponentScan("asia.sweethome") 至关重要：
// AuthGlobalFilter / JwtVerifier / CorsConfig 都在 asia.sweethome.{filter,util,config} 下，
// 是 asia.sweethome.gateway 的兄弟包而非子包 —— @SpringBootApplication 默认只扫描自身所在包
// 及子包，不显式声明 basePackages 的话，这三个类完全不会被注册为 Bean，网关会在没有任何报错的
// 情况下悄悄放行所有请求（不校验 token，也不注入 X-User-Id），是一个严重的安全隐患。
@SpringBootApplication
@ComponentScan(basePackages = "asia.sweethome")
@Slf4j
public class GatewayApplication {
    public static void main(String[] args) throws UnknownHostException {
        // 启动 Spring 容器
        ConfigurableApplicationContext app = SpringApplication.run(GatewayApplication.class, args);
        ConfigurableEnvironment env = app.getEnvironment();
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        log.info("--/\n---------------------------------------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}\n\t" +
                        "External: \t{}://{}:{}\n\t" +
                        "Profile(s): \t{}" +
                        "\n---------------------------------------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getActiveProfiles());

    }
}
