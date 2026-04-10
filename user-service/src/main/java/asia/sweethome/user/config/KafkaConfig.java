package asia.sweethome.user.config;

import asia.sweethome.user.entity.po.OutboxMessage;
import asia.sweethome.user.service.IOutboxMessagesService;
import asia.sweethome.user.util.RedisDistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

import static asia.sweethome.user.constant.RedisConstants.KEY_OUTBOX_RELAY_LOCK;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/10/2026 9:20 PM
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class KafkaConfig {



}
