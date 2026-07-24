package asia.sweethome.redpacket.config;

import static asia.sweethome.redpacket.constant.RedisConstant.GRAB_CONSUMER_GROUP;
import static asia.sweethome.redpacket.constant.RedisConstant.KEY_STREAM_REDPACKET_GRAB_OUTBOX;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/24/2026 11:35 上午
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedpacketStreamInitializer implements ApplicationRunner {

    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 创建消费者组
        try {
            stringRedisTemplate.opsForStream().createGroup(
                    KEY_STREAM_REDPACKET_GRAB_OUTBOX,
                    ReadOffset.from("0"),
                    GRAB_CONSUMER_GROUP
            );
        } catch (RedisSystemException e) {
            Throwable cause = e.getCause();
            // 异常必须精准捕获
            if (cause != null && cause.getMessage() != null && cause.getMessage().contains ("BUSYGROUP")) {
                log.info("消费组已经存在或稍后创建，跳过：{}", GRAB_CONSUMER_GROUP);
            } else {
                throw e;
            }
        }
    }

}
