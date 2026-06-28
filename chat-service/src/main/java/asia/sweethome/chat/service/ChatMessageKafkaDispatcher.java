package asia.sweethome.chat.service;

import static asia.sweethome.common.constants.ConversationTypeConstants.DIRECT;
import static asia.sweethome.common.constants.ConversationTypeConstants.GROUP;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.chat.entity.po.Conversation;
import asia.sweethome.chat.entity.po.ConversationMember;
import asia.sweethome.chat.entity.po.Message;
import asia.sweethome.chat.ws.registry.OnlineUserRegistry;
import asia.sweethome.common.constants.KafkaTopicConstants;
import asia.sweethome.common.entity.ko.ChatOfflineNotifyKO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: 负责将消息发送给离线的用户，Kafka 推送消息给 user-service 的 推送服务
 * @author: LOCRIAN_V
 * @date: 7/18/2026 10:08 下午
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageKafkaDispatcher {

    private final OnlineUserRegistry onlineUserRegistry;

    private final IConversationMembersService conversationMembersService;

    private final IConversationsService conversationsService;

    @DubboReference
    private UserApi userApi;

    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String,String> kafkaTemplate;

    public void dispatch(
            Message message
    ) {

        // 找到 发送方所在的群组中的成员
        List<ConversationMember> activeMembers = conversationMembersService.listActiveMembers(
                message.getConversationId()
        );

        List<ConversationMember> offlineConversationMembers = new LinkedList<>();

        // 先完成离线用户的 collect
        for (ConversationMember member : activeMembers) {

            Long receiverUserId = member.getUserId();

            // 跳过发送者自己
            if (receiverUserId.equals(
                    message.getSenderId()
            )) {
                continue;
            }

            // 跳过在线用户
            // todo 这是N次查询，需要优化
            if (onlineUserRegistry.isOnline(receiverUserId)) {
                continue;
            }

            offlineConversationMembers.add(
                    member
            );
        }

        // 拿到本对话的 Conversation
        Conversation thisConversation = conversationsService.getById(message.getConversationId());

        // 得到 发送者的 UserDTO
        UserDTO senderUserDTO = userApi.findUserById(message.getSenderId());

        // 组装 ko

        ChatOfflineNotifyKO ko = new ChatOfflineNotifyKO();

        ko.setSenderUserId(
                senderUserDTO.getId()
        );

        ko.setSenderUsername(
                senderUserDTO.getName()
        );

        ko.setSenderUserAvatarUrl(
                senderUserDTO.getAvatarUrl()
        );

        switch( thisConversation.getType() ){
            case GROUP -> ko.setConversationTitle(
                    thisConversation.getName()
            );
            case DIRECT -> ko.setConversationTitle( ko.getSenderUsername() );
        }

        ko.setMessageContent( message.getContent() );

        // Kafka
        // 逐个发送 Kafka 消息
        for (ConversationMember member : offlineConversationMembers) {

            ko.setReceiverUserId( member.getUserId() );

            String payload ;
            try {
                payload = objectMapper.writeValueAsString(ko);
            } catch (JsonProcessingException e) {
                log.warn("无法解析 ko 对象为 json", e);
                continue;
            }

            kafkaTemplate.send(
                    KafkaTopicConstants.TOPIC_CHAT_MESSAGE_OFFLINE,
                    payload
            );

        }

    }

}
