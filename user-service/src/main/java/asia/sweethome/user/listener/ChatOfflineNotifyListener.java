package asia.sweethome.user.listener;

import static asia.sweethome.common.constants.KafkaTopicConstants.TOPIC_CHAT_MESSAGE_OFFLINE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

import asia.sweethome.api.UserApi;
import asia.sweethome.common.entity.ko.ChatOfflineNotifyKO;
import asia.sweethome.user.entity.po.PushToken;
import asia.sweethome.user.push.JPushSender;
import asia.sweethome.user.service.IPushTokensService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: 处理消息推送给离线用户
 * @author: LOCRIAN_V
 * @date: 7/19/2026 12:07 上午
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ChatOfflineNotifyListener {

    private final ObjectMapper objectMapper;

    private final IPushTokensService pushTokensService;

    private final JPushSender jPushSender;

    @DubboReference
    private UserApi userApi;

    @KafkaListener(topics = TOPIC_CHAT_MESSAGE_OFFLINE )
    void topicChatMessageOffline( String messagePayload ){

        ChatOfflineNotifyKO ko;
        try {
            ko = objectMapper.readValue(messagePayload, ChatOfflineNotifyKO.class);
        } catch (JsonProcessingException e) {
            // 几乎不可能发生，所以只做日志处理
            log.warn("ObjectMapper 消息解析出错", e);
            return;
        }

        // 拿到ko，发起推送
        Long receiverUserId = ko.getReceiverUserId();

        // 对 receiver 做检查，看看是否真的存在
        try {
            userApi.findUserById(ko.getReceiverUserId());
        } catch (Exception e) {
            log.warn("查询目标用户失败，放弃本次推送 tagetUserId={}", ko.getReceiverUserId(), e);
            return;
        }


        List<PushToken> pushTokens = pushTokensService.lambdaQuery().eq(
                PushToken::getUserId,
                receiverUserId
        ).list();

        log.info(
                "用户 {} 向离线用户 {} 开始发出推送消息 {}",
                ko.getSenderUserId(),
                ko.getReceiverUserId(),
                ko.getMessageContent()
        );

        String messageTitle = ko.getConversationTitle();
        String messageContent = ko.getMessageContent();

        for (PushToken pushToken : pushTokens) {
            jPushSender.pushToDevice(
                    pushToken.getRegistrationId(),
                    messageTitle,
                    messageContent
            );
            log.info("已经完成对设备{}的推送", pushToken.getRegistrationId());
        }


    }

}
