package asia.sweethome.user;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 【user-service 启动类】
 * <p>
 * {@code @EnableDubbo} 开启 Dubbo：既能对外暴露本服务的 @DubboService（如 UserApiImpl），
 * 也能引用别人的 @DubboReference（如 FamilyApi）。
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 4:28 PM
 */
@SpringBootApplication
@EnableDubbo
@Slf4j
@MapperScan("asia.sweethome.user.mapper")
public class UserApplication {
    public static void main(String[] args) throws UnknownHostException {
        // 启动 Spring 容器
        ConfigurableApplicationContext app = SpringApplication.run(UserApplication.class);
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
