package asia.sweethome.redpacket.task;

import static asia.sweethome.redpacket.constant.RedisConstant.*;
import static asia.sweethome.redpacket.constant.ScheduleConstant.DELAY_REDPACKET_GRAB_CONSUME_PENDING_MSG_MS;
import static asia.sweethome.redpacket.constant.ScheduleConstant.DELAY_REDPACKET_GRAB_REDIS_2_DB_MS;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import asia.sweethome.redpacket.service.IRedpacketGrabsService;
import asia.sweethome.redpacket.service.IRedpacketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: 将redis中的入库消息进行消费
 * @author: LOCRIAN_V
 * @date: 7/24/2026 12:05 下午
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class RedpacketGrabRedis2DBConsumer {

    private final IRedpacketService redpacketService;
    private final IRedpacketGrabsService redpacketGrabsService;
    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedDelay = DELAY_REDPACKET_GRAB_REDIS_2_DB_MS)
    public void redpacketGrabRedis2DB() {

        List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream()
                .read(
                        Consumer.from(GRAB_CONSUMER_GROUP, CONSUMER_NAME),
                        StreamReadOptions.empty().count(10),
                        StreamOffset.create(
                                KEY_STREAM_REDPACKET_GRAB_OUTBOX,
                                ReadOffset.lastConsumed()
                                // 给我这个组从没投递过的新消息
                        )
                );

        // 没收到消息就忽略
        if (records == null || records.isEmpty()) {
            return;
        }

        for (MapRecord<String, Object, Object> record : records) {
            handleRecord(record);
        }

    }

    @Scheduled(fixedDelay = DELAY_REDPACKET_GRAB_CONSUME_PENDING_MSG_MS )
    public void redpacketGrabConsumePendingMsg(){

        /**
         * 	PendingMessages pending(K key, Consumer consumer, Range<?> range, long count);
         */

        PendingMessages pendingMessages = stringRedisTemplate.opsForStream().pending(
                KEY_STREAM_REDPACKET_GRAB_OUTBOX,
                GRAB_CONSUMER_GROUP,
                Range.unbounded(),
                100
        );

        for (PendingMessage pm : pendingMessages) {

            Duration idle = pm.getElapsedTimeSinceLastDelivery();
            if (idle.getSeconds() < MSG_MAX_PENDING_TIME_S) {
                continue; // 卡的不够久，跳过
            }

            RecordId id = pm.getId();

            log.info("⚠️发现了卡住的pending消息:{}，已 idle {} 秒", id, pm.getElapsedTimeSinceLastDelivery().getSeconds());

            // 认领（claim） + 重放
            List<MapRecord<String, Object, Object>> claimed = stringRedisTemplate.opsForStream().claim(
                    KEY_STREAM_REDPACKET_GRAB_OUTBOX,
                    GRAB_CONSUMER_GROUP,
                    CONSUMER_NAME,
                    Duration.ofSeconds(60),
                    pm.getId()
            );

            for (MapRecord<String, Object, Object> record : claimed) {
                handleRecord(record);
            }


        }
    }

    private void handleRecord( MapRecord<String, Object, Object> record ){

        log.info("收到了 record 消息 {}", record);

        // 2db

        Map<Object, Object> recordKvmap = record.getValue();

        try {
            redpacketGrabsService.persistGrab(
                    Long.valueOf((String) recordKvmap.get("redpacketId")),
                    Long.valueOf((String) recordKvmap.get("userId")),
                    Long.valueOf((String) recordKvmap.get("amount"))
            );
        } catch ( DuplicateKeyException e) {
            // 发现幂等，忽略，确认
        }

        // 响应消息
        stringRedisTemplate.opsForStream().acknowledge(
                KEY_STREAM_REDPACKET_GRAB_OUTBOX,
                GRAB_CONSUMER_GROUP,
                record.getId()
        );

    }

    // todo 定时清理idle时间过长且没有pending消息的的消费者

}
