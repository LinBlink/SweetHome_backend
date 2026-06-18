package asia.sweethome.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.jpush.api.JPushClient;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/17/2026 3:13 下午
 */

@Configuration
public class JPushConfig {

    @Value("${sh.jpush.app-key}")
    private String appKey;

    @Value("${sh.jpush.master-secret}")
    private String masterSecret;

    @Bean
    public JPushClient jPushClient() {
        return new JPushClient(
                masterSecret, appKey
        );
    }


}
