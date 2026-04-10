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
 * 【聊天数据组装器】
 * <p>
 * 数据库里的消息/会话只存了 id 之类的「原料」（比如 senderId），但前端要显示的是
 * 「发送者昵称、头像、我该怎么称呼他」。这个类负责把原料「补全」成前端可直接展示的 VO，
 * 需要的额外数据通过 Dubbo 去 user-service（昵称头像）和 family-service（称谓）取。
 * <p>
 * 关键点：同一条消息，不同接收者看到的称谓不同（我叫他「爸」，我弟叫他也「爸」但我妈叫他「老公」），
 * 所以组装必须带上 viewerUserId「以谁的视角」，不能缓存成一份通用结果。
 */
@Component
@RequiredArgsConstructor
public class ChatAssembler {

    private final IConversationMembersService conversationMembersService;
    private final IMessagesService messagesService;

    @DubboReference
    private UserApi userApi;      // 远程取昵称/头像

    @DubboReference
    private FamilyApi familyApi;  // 远程算称谓

    /** 把一条消息组装成「以 viewerUserId 视角」的展示对象 */
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

        return vo;
    }

    /**
     * 把一个会话组装成会话列表里的一项。除了基本信息，还要算出：
     * 成员数、我的未读数、最后一条消息预览；若是单聊，还要显示「对方」的名字/头像/我对他的称谓。
     */
    public ConversationVO toConversationVO(Conversation conversation, Long viewerUserId, String acceptLanguage) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conversation.getId());
        vo.setType(conversation.getType());
        vo.setFamilyId(conversation.getFamilyId());
        vo.setLastMessageAt(conversation.getLastMessageAt());

        List<ConversationMember> activeMembers = conversationMembersService.listActiveMembers(conversation.getId());
        vo.setMemberCount(activeMembers.size());

        // 未读数 = 我上次读到的那条消息之后，又新增了多少条
        ConversationMember viewerMembership = activeMembers.stream()
                .filter(m -> m.getUserId().equals(viewerUserId))
                .findFirst()
                .orElse(null);
        long unread = messagesService.countUnread(
                conversation.getId(),
                viewerMembership == null ? null : viewerMembership.getLastReadMessageId()
        );
        vo.setUnreadCount(unread);

        // 会话列表里显示的「最后一条消息」预览
        if (conversation.getLastMessageId() != null) {
            Message lastMessage = messagesService.getById(conversation.getLastMessageId());
            if (lastMessage != null) {
                vo.setLastMessage(lastMessage.getContent());
            }
        }

        if ("direct".equals(conversation.getType())) {
            // 单聊：会话名/头像应显示为「对方」——从成员里挑出不是我自己的那个人
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
                vo.setAvatarColor(AvatarUtil.color(counterpartId));
            }
        } else {
            // 群聊：直接用群名，头像用一个「家」字占位
            vo.setName(conversation.getName());
            vo.setAvatarLabel("家");
            vo.setAvatarColor(AvatarUtil.color(conversation.getId()));
        }

        return vo;
    }
}
