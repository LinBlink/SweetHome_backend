package asia.sweethome.health.task;

import static asia.sweethome.health.constant.RedisConstants.KEY_REMINDER_SCHEDULER_LOCK;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import asia.sweethome.common.constants.KafkaTopicConstants;
import asia.sweethome.common.entity.ko.HealthReminderNotifyKO;
import asia.sweethome.health.entity.po.HealthReminder;
import asia.sweethome.health.service.IHealthReminderService;
import asia.sweethome.health.util.RedisDistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 每分钟扫描一次「到点该提醒但今天还没提醒过」的成员，发 Kafka 触发极光推送。
 * 用 Redis 分布式锁保证 health-service 横向扩容时，同一时刻只有一个实例在扫描，
 * 不然每个实例都会各发一遍，用户会收到重复的提醒推送。
 *
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HealthReminderScheduler {

    private final IHealthReminderService healthReminderService;

    private final RedisDistributedLockUtil redisDistributedLockUtil;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 * * * * *")
    public void scanAndRemind() {

        boolean getLockSuccess = redisDistributedLockUtil.tryLock(KEY_REMINDER_SCHEDULER_LOCK);
        if (!getLockSuccess) {
            return;
        }

        try {
            LocalTime now = LocalTime.now().withSecond(0).withNano(0);
            LocalDate today = LocalDate.now();

            List<HealthReminder> enabledReminders = healthReminderService.lambdaQuery()
                    .eq(HealthReminder::getEnabled, true)
                    .list();

            for (HealthReminder reminder : enabledReminders) {

                LocalTime remindTime = reminder.getRemindTime();
                if (remindTime == null) {
                    continue;
                }

                boolean timeMatched = remindTime.getHour() == now.getHour() && remindTime.getMinute() == now.getMinute();
                if (!timeMatched) {
                    continue;
                }

                boolean alreadyRemindedToday = today.equals(reminder.getLastRemindedDate());
                if (alreadyRemindedToday) {
                    continue;
                }

                HealthReminderNotifyKO ko = new HealthReminderNotifyKO();
                ko.setUserId(reminder.getUserId());
                ko.setTitle("健康记录提醒");
                ko.setContent("该记录今天的身高体重血压啦～");

                String payload;
                try {
                    payload = objectMapper.writeValueAsString(ko);
                } catch (JsonProcessingException e) {
                    log.warn("无法解析健康提醒 ko 对象为 json, userId={}", reminder.getUserId(), e);
                    continue;
                }

                kafkaTemplate.send(KafkaTopicConstants.TOPIC_HEALTH_REMINDER_TRIGGERED, payload);

                healthReminderService.lambdaUpdate()
                        .eq(HealthReminder::getId, reminder.getId())
                        .set(HealthReminder::getLastRemindedDate, today)
                        .set(HealthReminder::getUpdatedAt, LocalDateTime.now())
                        .update();
            }
        } finally {
            redisDistributedLockUtil.unLock(KEY_REMINDER_SCHEDULER_LOCK);
        }
    }

}
