package asia.sweethome.chat.service.impl;

import asia.sweethome.chat.entity.po.ConversationMember;
import asia.sweethome.chat.mapper.ConversationMembersMapper;
import asia.sweethome.chat.service.IConversationMembersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 【会话成员 服务实现类】
 * <p>
 * 管理「谁在哪个会话里、读到哪了」。这里用 leftAt（退出时间）做「软退出」：
 * leftAt 为 null = 仍在会话中；有值 = 已退出。退出不删记录，方便日后重新加入时保留历史。
 */
@Service
public class ConversationMembersServiceImpl extends ServiceImpl<ConversationMembersMapper, ConversationMember> implements IConversationMembersService {

    /** 是否是会话的活跃成员 */
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

    /**
     * 加入会话（幂等）：
     * 从没加过 → 新建一条；曾经退出过（leftAt 有值）→ 清空 leftAt 重新激活；已在里面 → 什么都不做。
     * 幂等意味着「重复调用结果一样」，这样上游联动逻辑就不用担心多调一次会出错。
     */
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
            existing.setLeftAt(null);                     // 复活：清掉退出标记
            existing.setJoinedAt(LocalDateTime.now());
            updateById(existing);
        }
    }

    /** 退出会话（幂等）：把 leftAt 置为当前时间；本就不在会话里则什么都不做 */
    @Override
    public void leave(Long conversationId, Long userId) {
        ConversationMember member = getActiveMember(conversationId, userId);
        if (member != null) {
            member.setLeftAt(LocalDateTime.now());
            updateById(member);
        }
    }

    /** 更新已读进度：记住该用户在此会话读到了哪条消息，用于算未读数 */
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
