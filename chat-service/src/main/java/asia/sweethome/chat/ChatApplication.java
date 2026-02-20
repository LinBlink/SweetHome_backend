package asia.sweethome.chat;

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
 * 【chat-service 启动类】
 * <p>
 * {@code @EnableDubbo} 暴露 ChatApiImpl、引用 UserApi/FamilyApi；{@code @MapperScan} 扫描 Mapper。
 * 本服务还额外承载 WebSocket 端点与 Redis 订阅（见 config 包）。
 */
@SpringBootApplication
@Slf4j
@EnableDubbo
@MapperScan("asia.sweethome.chat.mapper")
public class ChatApplication {
    public static void main(String[] args) throws UnknownHostException {
        // 启动 Spring 容器
        ConfigurableApplicationContext app = SpringApplication.run(ChatApplication.class);
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
