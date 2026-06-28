package asia.sweethome.health.constant;

import java.time.Duration;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
public class RedisConstants {
    // REMINDER
    public static final String KEY_REMINDER_SCHEDULER_LOCK = "health:reminder:scheduler:lock";

    // LOCK
    public static final String LOCK_VALUE = "locked";
    public static final Duration LOCK_DEFAULT_EXPIRE_TIME = Duration.ofSeconds(60);
}
