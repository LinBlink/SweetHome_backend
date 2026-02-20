package asia.sweethome.auth;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 【auth-service 启动类】
 * <p>
 * main 方法是整个微服务的入口。Spring Boot 会从这里启动：扫描组件、连接 Nacos、
 * 暴露 Dubbo 服务、监听端口……启动完成后打印访问地址方便本地调试。
 * <ul>
 *   <li>{@code @SpringBootApplication}：Spring Boot 应用总开关（含自动配置 + 组件扫描）；</li>
 *   <li>{@code @MapperScan}：告诉 MyBatis 去这个包下找 Mapper 接口并生成实现。</li>
 * </ul>
 *
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:59 PM
 */
@SpringBootApplication
@Slf4j
@MapperScan("asia.sweethome.auth.mapper")
public class AuthApplication {
    public static void main(String[] args) throws UnknownHostException {
        // 启动 Spring 容器，返回的 app 用来读取运行环境信息（端口、profile 等）
        ConfigurableApplicationContext app = SpringApplication.run(AuthApplication.class);
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
