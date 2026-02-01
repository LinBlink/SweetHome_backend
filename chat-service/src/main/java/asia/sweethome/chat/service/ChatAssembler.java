package asia.sweethome.chat.service;

import asia.sweethome.api.FamilyApi;
import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.RelationDTO;
import asia.sweethome.api.entity.dto.RelationQueryDTO;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.chat.entity.po.Conversation;
import asia.sweethome.chat.entity.po.ConversationMember;
import asia.sweethome.chat.entity.po.Message;
import asia.sweethome.chat.entity.vo.ConversationVO;
import asia.sweethome.chat.entity.vo.MessageVO;
import asia.sweethome.chat.util.AvatarUtil;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 把持久化实体组装成对外 VO：补齐发送者/对方昵称、头像视觉标识、
 * 以及相对当前查看者动态计算的亲属称谓（relationCode/relationLabel）。
 */
@Component
@RequiredArgsConstructor
public class ChatAssembler {

    private final IConversationMembersService conversationMembersService;
    private final IMessagesService messagesService;

    @DubboReference
    private UserApi userApi;

    @DubboReference
    private FamilyApi familyApi;

    public MessageVO toMessageVO(Message message, Long viewerUserId, String acceptLanguage) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setClientId(message.getClientId());
        vo.setConversationId(message.getConversationId());
        vo.setSenderId(message.getSenderId());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setSentAt(message.getSentAt());

        UserDTO sender = userApi.findUserById(message.getSenderId());
        if (sender != null) {
            vo.setSenderName(sender.getName());
            vo.setSenderAvatarLabel(AvatarUtil.label(sender.getName()));
        }

        RelationDTO relation = familyApi.getRelation(
                new RelationQueryDTO(viewerUserId, message.getSenderId(), acceptLanguage)
        );
        vo.setSenderRelationCode(relation.getRelationCode());
        vo.setSenderRelationLabel(relation.getRelationLabel());

        return vo;
    }

    public ConversationVO toConversationVO(Conversation conversation, Long viewerUserId, String acceptLanguage) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conversation.getId());
        vo.setType(conversation.getType());
        vo.setFamilyId(conversation.getFamilyId());
        vo.setLastMessageAt(conversation.getLastMessageAt());

        List<ConversationMember> activeMembers = conversationMembersService.listActiveMembers(conversation.getId());
        vo.setMemberCount(activeMembers.size());

        ConversationMember viewerMembership = activeMembers.stream()
                .filter(m -> m.getUserId().equals(viewerUserId))
                .findFirst()
                .orElse(null);
        long unread = messagesService.countUnread(
                conversation.getId(),
                viewerMembership == null ? null : viewerMembership.getLastReadMessageId()
        );
        vo.setUnreadCount(unread);

        if (conversation.getLastMessageId() != null) {
            Message lastMessage = messagesService.getById(conversation.getLastMessageId());
            if (lastMessage != null) {
                vo.setLastMessage(lastMessage.getContent());
            }
        }

        if ("direct".equals(conversation.getType())) {
            Long counterpartId = activeMembers.stream()
                    .map(ConversationMember::getUserId)
                    .filter(id -> !id.equals(viewerUserId))
                    .findFirst()
                    .orElse(null);

            if (counterpartId != null) {
                UserDTO counterpart = userApi.findUserById(counterpartId);
                if (counterpart != null) {
                    vo.setName(counterpart.getName());
                    vo.setAvatarLabel(AvatarUtil.label(counterpart.getName()));
                }
                RelationDTO relation = familyApi.getRelation(
                        new RelationQueryDTO(viewerUserId, counterpartId, acceptLanguage)
                );
                vo.setRelationCode(relation.getRelationCode());
                vo.setRelationLabel(relation.getRelationLabel());
                vo.setAvatarColor(AvatarUtil.color(counterpartId));
            }
        } else {
            vo.setName(conversation.getName());
            vo.setAvatarLabel("家");
            vo.setAvatarColor(AvatarUtil.color(conversation.getId()));
        }

        return vo;
    }
}
