package asia.sweethome.chat.service.impl;

import asia.sweethome.chat.entity.po.ConversationMember;
import asia.sweethome.chat.mapper.ConversationMembersMapper;
import asia.sweethome.chat.service.IConversationMembersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationMembersServiceImpl extends ServiceImpl<ConversationMembersMapper, ConversationMember> implements IConversationMembersService {

    @Override
    public boolean isActiveMember(Long conversationId, Long userId) {
        return getActiveMember(conversationId, userId) != null;
    }

    @Override
    public ConversationMember getActiveMember(Long conversationId, Long userId) {
        return lambdaQuery()
                .eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getUserId, userId)
                .isNull(ConversationMember::getLeftAt)
                .one();
    }

    @Override
    public List<ConversationMember> listActiveMembers(Long conversationId) {
        return lambdaQuery()
                .eq(ConversationMember::getConversationId, conversationId)
                .isNull(ConversationMember::getLeftAt)
                .list();
    }

    @Override
    public List<Long> listActiveConversationIds(Long userId) {
        return lambdaQuery()
                .eq(ConversationMember::getUserId, userId)
                .isNull(ConversationMember::getLeftAt)
                .list()
                .stream()
                .map(ConversationMember::getConversationId)
                .toList();
    }

    @Override
    public void addOrReactivate(Long conversationId, Long userId) {
        ConversationMember existing = lambdaQuery()
                .eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getUserId, userId)
                .one();

        if (existing == null) {
            ConversationMember member = new ConversationMember();
            member.setConversationId(conversationId);
            member.setUserId(userId);
            member.setJoinedAt(LocalDateTime.now());
            save(member);
            return;
        }

        if (existing.getLeftAt() != null) {
            existing.setLeftAt(null);
            existing.setJoinedAt(LocalDateTime.now());
            updateById(existing);
        }
    }

    @Override
    public void leave(Long conversationId, Long userId) {
        ConversationMember member = getActiveMember(conversationId, userId);
        if (member != null) {
            member.setLeftAt(LocalDateTime.now());
            updateById(member);
        }
    }

    @Override
    public void markRead(Long conversationId, Long userId, Long lastReadMessageId) {
        ConversationMember member = lambdaQuery()
                .eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getUserId, userId)
                .one();
        if (member != null) {
            member.setLastReadMessageId(lastReadMessageId);
            member.setLastReadAt(LocalDateTime.now());
            updateById(member);
        }
    }
}
