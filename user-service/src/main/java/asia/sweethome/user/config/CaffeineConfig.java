package asia.sweethome.user.config;

import asia.sweethome.api.entity.dto.UserDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static asia.sweethome.user.constant.CaffeineConstants.USER_DTO_CACHE_EXPIRE_AFTER_WRITE_SECOND;
import static asia.sweethome.user.constant.CaffeineConstants.USER_DTO_CACHE_MAX_SIZE;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/7/2026 11:47 下午
 */
@Configuration
public class CaffeineConfig {

    @Bean
    Cache<Long, Optional<UserDTO>> userDTOCache(){
        return Caffeine.newBuilder()
                .maximumSize( USER_DTO_CACHE_MAX_SIZE ) // 根据内存大小和可能的并发数决定
                .expireAfterWrite(USER_DTO_CACHE_EXPIRE_AFTER_WRITE_SECOND, TimeUnit.SECONDS) // 用户信息更新后最多30秒完成刷新
                .build();
    }

}
