package asia.sweethome.user.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/12/2026 9:03 PM
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SweetHome API - User Service",
                version = "1.0.0",
                description = "SweetHome 项目 API文档 - 用户服务"
        )
)
public class OpenAPIConfig {
}
