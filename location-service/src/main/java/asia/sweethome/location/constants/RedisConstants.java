package asia.sweethome.location.constants;

import java.time.Duration;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/8/2026 11:03 上午
 */
public class RedisConstants {
    // OUTBOX
    public static final String KEY_OUTBOX_RELAY_LOCK = "outbox:relay:lock";

    // LOCK
    public static final String LOCK_VALUE = "locked";
    public static final Duration LOCK_DEFAULT_EXPIRE_TIME = Duration.ofSeconds(60);
}