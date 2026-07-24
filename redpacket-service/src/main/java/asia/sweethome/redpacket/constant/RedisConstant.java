package asia.sweethome.redpacket.constant;

import java.time.Duration;
import java.util.UUID;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/8/2026 11:03 上午
 */
public class RedisConstant {

    // REDPACKET_ALLOCATION
    public static final String KEY_LIST_REDPACKET_ALLOCATION = "redpacket:allocation";
    public static final Duration KEY_LIST_REDPACKET_ALLOCATION_TTL = Duration.ofHours(24);
    public static final String KEY_HASH_GRABBED_USERS = "redpacket:grabbed_users";
    public static final Long USER_ALREADY_GRABBED = -1L;
    public static final Long REDPACKET_EMPTY = 0L;

    // REDPACKET_ALLOCATION_STREAM_OUTBOX
    public static final String KEY_STREAM_REDPACKET_GRAB_OUTBOX = "redpacket:grab:outbox";
    public static final String GRAB_CONSUMER_GROUP = "redpacket_grab_consumer_group";
    public static final String CONSUMER_NAME = "consumer_" + UUID.randomUUID(); // 每个微服务不同
    public static final int MSG_MAX_PENDING_TIME_S = 10;


    // LOCK
    public static final String LOCK_VALUE = "locked";
    public static final Duration LOCK_DEFAULT_EXPIRE_TIME = Duration.ofSeconds(60);
    public static final String KEY_SWEEP_EXPIRED_REDPACKET_LOCK = "sweep:expired_redpacket:lock";

}