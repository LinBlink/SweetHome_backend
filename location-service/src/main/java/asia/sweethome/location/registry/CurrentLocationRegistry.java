package asia.sweethome.location.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.location.entity.ro.CurrentLocationRO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: 缓存用户当前所在位置
 * @author: LOCRIAN_V
 * @date: 7/13/2026 5:35 PM
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentLocationRegistry {

    private static final String KEY_PREFIX = "location:current:";
    private static final Duration KEY_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    /**
     *
     * @param userId
     * @param currentLocationRO
     */
    public void updateCurrent(Long userId, CurrentLocationRO currentLocationRO) {

        if (userId == null || currentLocationRO == null) {
            // 这里用 IllegalArgumentException 而不是 BusinessException：
            // 这是「调用方传参就不合法」的程序错误，不是业务规则失败，不需要被 GlobalExceptionHandler 转成 Result 返回给前端
            throw new IllegalArgumentException("userId 和 currentLocationRO 不能为空");
        }

        String currentLocationROJSON;
        try {
            currentLocationROJSON = objectMapper.writeValueAsString(currentLocationRO);
        } catch (JsonProcessingException e) {
            log.error("位置对象序列化失败：userId={}", userId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        redisTemplate.opsForValue()
                .set(
                        KEY_PREFIX + userId,
                        currentLocationROJSON,
                        KEY_TTL
                );
    }

    /**
     * 返回 redis 中的 currentLocationRO 对象
     *
     * @param userId
     * @return 返回RO，由service拼好返回前端
     */
    public CurrentLocationRO getCurrent(Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }

        String currentLocationJSON = redisTemplate.opsForValue().get(KEY_PREFIX + userId);

        if (currentLocationJSON == null) {
            // key 不存在或已过期（TTL 10 分钟到了），不是异常情况——交给调用方决定怎么展示"该成员当前无位置数据"
            return null;
        }

        CurrentLocationRO currentLocationRO;
        try {
            currentLocationRO = objectMapper.readValue(currentLocationJSON, CurrentLocationRO.class);
        } catch (JsonProcessingException e) {
            log.error("位置对象反序列化失败：userId={}, json={}", userId, currentLocationJSON, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        return currentLocationRO;

    }

}

