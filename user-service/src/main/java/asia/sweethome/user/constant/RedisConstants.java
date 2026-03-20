package asia.sweethome.user.constant;

import java.time.Duration;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/8/2026 11:03 上午
 */
public class RedisConstants {
    // UserDTO CACHE
    public static final String USER_DTO_CACHE_KEY = "UserService:UserDTO";
    public static final Duration USER_DTO_NULL_TTL = Duration.ofSeconds(10); // 空值TTL短，主要防穿透
    public static final Duration USER_DTO_TTL = Duration.ofSeconds(30); // 非空TTL长，用户信息更新间隔
    public static String userDTOCacheKey( Long userId ){
        return USER_DTO_CACHE_KEY + ":" + userId;
    }

}
