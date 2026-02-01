package asia.sweethome.chat.service.dubbo;

import asia.sweethome.api.ChatApi;
import asia.sweethome.chat.entity.po.Conversation;
import asia.sweethome.chat.service.IConversationMembersService;
import asia.sweethome.chat.service.IConversationsService;
import asia.sweethome.chat.ws.OnlineUserRegistry;
import asia.sweethome.common.constants.ConversationTypeConstants;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@DubboService
@RequiredArgsConstructor
public class ChatApiImpl implements ChatApi {

    private final IConversationsService conversationsService;
    private final IConversationMembersService conversationMembersService;
    private final OnlineUserRegistry onlineUserRegistry;

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

    @Override
    public void addMemberToGroupConversation(Long familyId, Long userId) {
        Conversation group = groupConversationOf(familyId);
        if (group == null) {
            throw new BusinessException(ErrorCode.NO_SUCH_CONVERSATION);
        }
        conversationMembersService.addOrReactivate(group.getId(), userId);
    }

    @Override
    public void removeMemberFromGroupConversation(Long familyId, Long userId) {
        Conversation group = groupConversationOf(familyId);
        if (group == null) {
            // 家庭本身可能已经在级联中被软删除，幂等放行
            return;
        }
        conversationMembersService.leave(group.getId(), userId);
    }

    @Override
    public List<Long> filterOnlineUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return onlineUserRegistry.filterOnline(userIds);
    }

    private Conversation groupConversationOf(Long familyId) {
        return conversationsService.lambdaQuery()
                .eq(Conversation::getFamilyId, familyId)
                .eq(Conversation::getType, ConversationTypeConstants.GROUP)
                .isNull(Conversation::getDeletedAt)
                .one();
    }
}
