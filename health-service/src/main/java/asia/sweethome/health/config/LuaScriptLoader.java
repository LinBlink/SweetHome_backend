package asia.sweethome.health.config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
@RequiredArgsConstructor
@Component
public class LuaScriptLoader {

    private final ResourceLoader resourceLoader;

    private RedisScript<Long> unlockScript;

    @PostConstruct
    public void init() {
        // 加载解锁脚本
        unlockScript = loadScript(
                "classpath:lua/unlock.lua",
                Long.class
        );
    }

    private <T> RedisScript<T> loadScript(
            String scriptPath,
            Class<T> resultType
    ) {

        try {
            Resource resource = resourceLoader.getResource(
                    scriptPath
            );

            String scriptContent = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            return new DefaultRedisScript<>(
                    scriptContent,
                    resultType
            );
        } catch (IOException e) {
            throw new RuntimeException("加载lua脚本失败");
        }

    }

    public RedisScript<Long> getUnlockScript() {
        return unlockScript;
    }

}
