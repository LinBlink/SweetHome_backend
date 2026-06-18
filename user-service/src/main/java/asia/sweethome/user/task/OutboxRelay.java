package asia.sweethome.user.task;

import static asia.sweethome.user.constant.RedisConstants.KEY_OUTBOX_RELAY_LOCK;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import asia.sweethome.user.entity.po.OutboxMessage;
import asia.sweethome.user.service.IOutboxMessagesService;
import asia.sweethome.user.util.RedisDistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/10/2026 10:41 PM
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final IOutboxMessagesService outboxMessagesService;
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final RedisDistributedLockUtil redisDistributedLock;

    // 每隔3秒钟检查 outbox 消息
    @Scheduled(fixedDelay = 3000)
    public void relay(){
        // 防止同一时刻不同线程一起消费，必须加锁
        boolean getLockSuccess = redisDistributedLock.tryLock(
                KEY_OUTBOX_RELAY_LOCK
        );

        // 如果已经锁了，就等下次
        if (!getLockSuccess) {
            return;
        }

        // 只要一加锁就来个大 try
        try {

            // log.info("✉️ LOCKBOX 查阅有无未发信件");

            List<OutboxMessage> outboxMessageList = outboxMessagesService.lambdaQuery()
                    .eq(OutboxMessage::getStatus,
                            OutboxMessage.STATUS_UNSEND)
                    .orderByAsc(
                            OutboxMessage::getId
                    ).last("LIMIT 100")
                    .list();

            for (OutboxMessage msg : outboxMessageList) {
                try {
                    // 发送
                    kafkaTemplate.send(
                            msg.getTopic(),
                            msg.getPayload(),
                            msg.getPayload()
                    ).get(); // get : 同步等确认

                    // 发送完成，将 status 改为1表示已发送
                    outboxMessagesService.lambdaUpdate()
                            .eq(
                                    OutboxMessage::getId,
                                    msg.getId()
                            )
                            .set(
                                    OutboxMessage::getStatus,
                                    OutboxMessage.STATUS_SENT
                            )
                            .update();

                } catch (Exception e) {
                    // 发送失败，retry次数+1，等着下轮重试

                    outboxMessagesService.lambdaUpdate()
                            .eq( OutboxMessage::getId,
                                    msg.getId())
                            .set(
                                    OutboxMessage::getRetryCount,
                                    msg.getRetryCount()+1
                            ).update();

                    log.info("✉️ 消息发送失败，下次重试 ");


                    // throw new RuntimeException(e); 千万不能抛
                }
            }
        } finally {
            redisDistributedLock.unLock( KEY_OUTBOX_RELAY_LOCK );
        }


    }

}
