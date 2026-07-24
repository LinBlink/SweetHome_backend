package asia.sweethome.redpacket.util;

import static asia.sweethome.redpacket.constant.RedisConstant.LOCK_DEFAULT_EXPIRE_TIME;
import static asia.sweethome.redpacket.constant.RedisConstant.LOCK_VALUE;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

import asia.sweethome.redpacket.config.LuaScriptLoader;
import lombok.RequiredArgsConstructor;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/10/2026 9:36 PM
 */
@RequiredArgsConstructor
@Component
public class RedisDistributedLockUtil {

    private final StringRedisTemplate redisTemplate;

    private final LuaScriptLoader luaScriptLoader;

    public boolean tryLock(
            String lockKey
    ) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(
                        lockKey,
                        LOCK_VALUE,
                        LOCK_DEFAULT_EXPIRE_TIME
                );
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
