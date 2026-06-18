package asia.sweethome.user.listener;

import static asia.sweethome.common.constants.KafkaTopicConstants.TOPIC_FENCE_ALARM_TRIGGERED;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

import asia.sweethome.api.UserApi;
import asia.sweethome.common.entity.ko.FenceAlarmMessageKO;
import asia.sweethome.user.entity.po.PushToken;
import asia.sweethome.user.push.JPushSender;
import asia.sweethome.user.service.IPushTokensService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/17/2026 1:57 下午
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class FenceAlarmNotifyListener {

    private final ObjectMapper objectMapper;

    private final IPushTokensService pushTokensService;

    private final JPushSender jPushSender;

    @DubboReference
    private UserApi userApi;

    /**
     * 收到用户越过围栏，进行报警推送
     */
    @KafkaListener(topics = TOPIC_FENCE_ALARM_TRIGGERED)
    void topicFenceAlarmTriggeredListener(String messagePayload) {

        FenceAlarmMessageKO fenceAlarmMessageKO;

        try {
            fenceAlarmMessageKO = objectMapper.readValue(
                    messagePayload,
                    FenceAlarmMessageKO.class
            );
        } catch (JsonProcessingException e) {
            // 几乎不可能发生，所以只做日志处理
            log.warn("ObjectMapper 消息解析出错", e);
            return;
        }

        Long setterUserId = fenceAlarmMessageKO.getSetterUserId();

        List<PushToken> setterUsersPushTokens = pushTokensService.lambdaQuery()
                .eq(
                        PushToken::getUserId,
                        setterUserId
                ).list();

        log.info(
                "开始调用推送服务商发送通知 {}", setterUsersPushTokens
        );


        String targetUsername = null;
        try {
            targetUsername = userApi.findUserById(fenceAlarmMessageKO.getTargetUserId()).getName();
        } catch (Exception e) {
            log.warn("查询目标用户失败，放弃本次推送 tagetUserId={}", fenceAlarmMessageKO.getTargetUserId(), e);
            return;
        }


        String msgTitle;
        String msgContent = "点击查看用户位置详情";

        switch (fenceAlarmMessageKO.getAlarmType()) {
            case FenceAlarmMessageKO.ALARM_TYPE_STEPPED_INSIDE:
                msgTitle = targetUsername + "进入了围栏 " + fenceAlarmMessageKO.getFenceName();
                break;
            case FenceAlarmMessageKO.ALARM_TYPE_STEPPED_OUTSIDE:
                msgTitle = targetUsername + "离开了围栏 " + fenceAlarmMessageKO.getFenceName();
                break;
            default:
                log.warn("收到的 AlarmType 值非法，值为 {}", fenceAlarmMessageKO.getAlarmType());
                return;
        }

        for (PushToken token : setterUsersPushTokens) {

            jPushSender.pushToDevice(
                    token.getRegistrationId(),
                    msgTitle,
                    msgContent
            );
        }


    }

}
