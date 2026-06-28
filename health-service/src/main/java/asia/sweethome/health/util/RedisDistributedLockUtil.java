package asia.sweethome.health.util;

import static asia.sweethome.health.constant.RedisConstants.LOCK_DEFAULT_EXPIRE_TIME;
import static asia.sweethome.health.constant.RedisConstants.LOCK_VALUE;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

import asia.sweethome.health.config.LuaScriptLoader;
import lombok.RequiredArgsConstructor;

/**
 * @description: Redis分布式锁工具
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
@RequiredArgsConstructor
@Component
public class RedisDistributedLockUtil {

    private final StringRedisTemplate redisTemplate;
    private final LuaScriptLoader luaScriptLoader;

    public boolean tryLock(
            String lockKey
    ) {

        Boolean result = redisTemplate.opsForValue().setIfAbsent(
                lockKey,
                LOCK_VALUE,
                LOCK_DEFAULT_EXPIRE_TIME
        );// 锁加超时防止一直持有

        return Boolean.TRUE.equals(result);

    }

    public boolean unLock(
            String lockKey
    ) {

        RedisScript<Long> unlockScript = luaScriptLoader.getUnlockScript();

        Long result = redisTemplate.execute(
                unlockScript,
                Collections.singletonList(
                        lockKey
                ),
                LOCK_VALUE
        );

        return Long.valueOf(1L).equals(result);

    }

}
