package asia.sweethome.moment;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/15/2026 3:55 PM
 */
@Slf4j
@SpringBootApplication
@EnableDubbo
@MapperScan("asia.sweethome.moment.mapper")
public class MomentApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext app = SpringApplication.run(MomentApplication.class);


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
