package asia.sweethome.user.listener;

import static asia.sweethome.common.constants.KafkaTopicConstants.TOPIC_USER_PROFILE_CHANGED;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

import asia.sweethome.api.entity.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/10/2026 12:35 上午
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserCacheInvalidateListener {


    private final Cache<Long, Optional<UserDTO>> userDTOCache;

    private final ObjectMapper objectMapper;


    /**
     * 收到失效消息，清除L1缓存。L2被清理过无需操作
     * @param userIdStr
     */
    @KafkaListener(topics = TOPIC_USER_PROFILE_CHANGED)
    void topicUserProfileChangedListener( String userIdStr ){
        log.info("🏠 收到失效消息，清空userId {} L1缓存", userIdStr);
        long userId = Long.parseLong(userIdStr);
        userDTOCache.invalidate( userId );
    }


}
