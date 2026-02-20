package asia.sweethome.family;

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
 * 【family-service 启动类】
 * <p>
 * {@code @EnableDubbo} 开启 Dubbo（暴露 FamilyApiImpl，同时引用 UserApi/ChatApi）；
 * {@code @MapperScan} 扫描 Mapper 接口。
 *
 * @author: LOCRIAN_V
 * @date: 7/2/2026 3:51 下午
 */
@SpringBootApplication
@Slf4j
@EnableDubbo
@MapperScan("asia.sweethome.family.mapper")
public class FamilyApplication {
    public static void main(String[] args) throws UnknownHostException {
        // 启动 Spring 容器
        ConfigurableApplicationContext app = SpringApplication.run(FamilyApplication.class);
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
