package asia.sweethome.location.task;

import static asia.sweethome.location.constants.RedisConstants.KEY_OUTBOX_RELAY_LOCK;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import asia.sweethome.location.entity.po.OutboxMessage;
import asia.sweethome.location.service.IOutboxMessagesService;
import asia.sweethome.location.util.RedisDistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/16/2026 11:05 下午
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final IOutboxMessagesService outboxMessagesService;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final RedisDistributedLockUtil redisDistributedLockUtil;

    @Scheduled(fixedDelay = 3000)
    public void relay() {

        boolean getLockSuccess = redisDistributedLockUtil.tryLock(
                KEY_OUTBOX_RELAY_LOCK
        );

        if (!getLockSuccess) {
            return;
        }

        try {
            // 只要加锁就要加一个大try
            List<OutboxMessage> outboxMessageList = outboxMessagesService.lambdaQuery()
                    .eq(
                            OutboxMessage::getStatus,
                            OutboxMessage.STATUS_UNSEND
                    ).orderByAsc(
                            OutboxMessage::getId
                    ).last("LIMIT 100").list();

            for (OutboxMessage msg : outboxMessageList) {

                try {
                    kafkaTemplate.send(
                            msg.getTopic(),
                            msg.getPayload(),
                            msg.getPayload()
                    ).get(); // get: 同步等确认

                    // 发送完成，将 status 改为1表示已经发送

                    outboxMessagesService.lambdaUpdate()
                            .eq(
                                    OutboxMessage::getId,
                                    msg.getId()
                            ).set(
                                    OutboxMessage::getStatus,
                                    OutboxMessage.STATUS_SENT
                            ).update();
                } catch (Exception e) {

                    outboxMessagesService.lambdaUpdate()
                            .eq(
                                    OutboxMessage::getId,
                                    msg.getId()
                            ).set(
                                    OutboxMessage::getRetryCount,
                                    msg.getRetryCount() + 1
                            ).update();
                    log.warn("📃 消息发送失败，下次重试", e);

                }

            }
        } finally {
            redisDistributedLockUtil.unLock(KEY_OUTBOX_RELAY_LOCK);
        }

    }


}
