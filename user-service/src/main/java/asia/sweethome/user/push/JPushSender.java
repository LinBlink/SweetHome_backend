package asia.sweethome.user.push;

import org.springframework.stereotype.Component;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/17/2026 3:05 下午
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JPushSender {

    private final JPushClient jPushClient;

    public void pushToDevice(
            String registrationId,
            String title,
            String content
    ) {

        PushPayload payload = PushPayload.newBuilder()
                .setAudience(
                        Audience.registrationId(registrationId)
                ).setPlatform(
                        Platform.all()
                ).setNotification(
                        Notification.alert(title)
                ).setMessage(
                        Message.content(content)
                ).build();

        try {
            jPushClient.sendPush(payload);
        } catch (Exception e) {
            log.error("📃 极光推送异常 ", e);
        }

    }

}
