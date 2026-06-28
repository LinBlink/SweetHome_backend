package asia.sweethome.user.listener;

import static asia.sweethome.common.constants.KafkaTopicConstants.TOPIC_HEALTH_REMINDER_TRIGGERED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

import asia.sweethome.common.entity.ko.HealthReminderNotifyKO;
import asia.sweethome.user.entity.po.PushToken;
import asia.sweethome.user.push.JPushSender;
import asia.sweethome.user.service.IPushTokensService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理 health-service 发来的「该记录健康数据了」提醒事件
 *
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class HealthReminderNotifyListener {

    private final ObjectMapper objectMapper;

    private final IPushTokensService pushTokensService;

    private final JPushSender jPushSender;

    @KafkaListener(topics = TOPIC_HEALTH_REMINDER_TRIGGERED)
    void topicHealthReminderTriggered(String messagePayload) {

        HealthReminderNotifyKO ko;
        try {
            ko = objectMapper.readValue(messagePayload, HealthReminderNotifyKO.class);
        } catch (JsonProcessingException e) {
            log.warn("ObjectMapper 消息解析出错", e);
            return;
        }

        List<PushToken> pushTokens = pushTokensService.lambdaQuery()
                .eq(PushToken::getUserId, ko.getUserId())
                .list();

        log.info("向用户 {} 发出健康记录提醒", ko.getUserId());

        for (PushToken pushToken : pushTokens) {
            jPushSender.pushToDevice(pushToken.getRegistrationId(), ko.getTitle(), ko.getContent());
            log.info("已经完成对设备{}的健康提醒推送", pushToken.getRegistrationId());
        }
    }

}
