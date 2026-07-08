package asia.sweethome.chat.service.dubbo;

import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import asia.sweethome.api.ChatApi;
import asia.sweethome.chat.entity.po.Conversation;
import asia.sweethome.chat.entity.po.ConversationMember;
import asia.sweethome.chat.service.IConversationMembersService;
import asia.sweethome.chat.service.IConversationsService;
import asia.sweethome.chat.ws.registry.OnlineUserRegistry;
import asia.sweethome.common.constants.ConversationTypeConstants;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

/**
 * 【ChatApi 的 Dubbo 实现】
 * <p>
 * chat-service 对外提供的远程接口，主要给 family-service 在「建家庭/进家庭/退家庭」时联动维护
 * 家庭群聊；另外也提供「筛在线用户」给成员列表用。所有方法都做成幂等，重复调用不会出错。
 */
@DubboService
@RequiredArgsConstructor
public class ChatApiImpl implements ChatApi {

    private final IConversationsService conversationsService;
    private final IConversationMembersService conversationMembersService;
    private final OnlineUserRegistry onlineUserRegistry;

    /** 建家庭时调用：创建一个群聊会话并拉入初始成员，返回会话 id */
    @Override
    public Long createGroupConversation(Long familyId, String name, List<Long> memberUserIds) {
        Conversation conversation = new Conversation();
        conversation.setType(ConversationTypeConstants.GROUP);
        conversation.setName(name);
        conversation.setFamilyId(familyId);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationsService.save(conversation);

        if (memberUserIds != null) {
            memberUserIds.forEach(userId -> conversationMembersService.addOrReactivate(conversation.getId(), userId));
        }

        return conversation.getId();
    }

    /** 有人加入家庭时调用：把该用户拉进家庭群聊（幂等，已在群里不会重复加） */
    @Override
    public void addMemberToGroupConversation(Long familyId, Long userId) {
        Conversation group = groupConversationOf(familyId);
        if (group == null) {
            throw new BusinessException(ErrorCode.NO_SUCH_CONVERSATION);
        }
        conversationMembersService.addOrReactivate(group.getId(), userId);
    }

    /** 有人退出家庭时调用：把该用户移出家庭群聊 */
    @Override
    public void removeMemberFromGroupConversation(Long familyId, Long userId) {
        Conversation group = groupConversationOf(familyId);
        if (group == null) {
            // 家庭本身可能已经在级联中被软删除，此时群聊也没了，幂等放行即可
            return;
        }
        conversationMembersService.leave(group.getId(), userId);
    }

    /** 从一批用户里筛出当前在线的（读全局在线名单） */
    @Override
    public List<Long> filterOnlineUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return onlineUserRegistry.filterOnline(userIds);
    }

    @Override
    public Long getConversationMemberCount(Long conversation) {
        return conversationMembersService.lambdaQuery()
                .eq(
                        ConversationMember::getConversationId,
                        conversation
                ).count();
    }

    /** 找某家庭对应的群聊会话（一个家庭一个群） */
    private Conversation groupConversationOf(Long familyId) {
        return conversationsService.lambdaQuery()
                .eq(Conversation::getFamilyId, familyId)
                .eq(Conversation::getType, ConversationTypeConstants.GROUP)
                .isNull(Conversation::getDeletedAt)
                .one();
    }

    @Override
    public Boolean userExistsInConversation(Long userId, Long conversationId) {

        ConversationMember one = conversationMembersService.lambdaQuery().eq(
                ConversationMember::getConversationId,
                conversationId
        ).eq(
                ConversationMember::getUserId,
                userId
        ).isNull(
                ConversationMember::getLeftAt
        ).one();

        return one != null;
    }

    @Override
    public Long getConversationFamilyId(Long conversationId) {

        Conversation one = conversationsService.lambdaQuery().eq(
                Conversation::getId,
                conversationId
        ).isNull(
                Conversation::getDeletedAt
        ).one();

        if (one == null || one.getFamilyId() == null) {
            return null;
        }

        return one.getFamilyId();

    }

}
