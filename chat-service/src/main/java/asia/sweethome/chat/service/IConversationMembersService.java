package asia.sweethome.chat.service;

import asia.sweethome.chat.entity.po.ConversationMember;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IConversationMembersService extends IService<ConversationMember> {

    boolean isActiveMember(Long conversationId, Long userId);

    ConversationMember getActiveMember(Long conversationId, Long userId);

    List<ConversationMember> listActiveMembers(Long conversationId);

    List<Long> listActiveConversationIds(Long userId);

    // 幂等：已是活跃成员则不重复插入；曾退出过（left_at 非空）则重新激活
    void addOrReactivate(Long conversationId, Long userId);

    // 幂等：非成员或已退出则什么都不做
    void leave(Long conversationId, Long userId);

    void markRead(Long conversationId, Long userId, Long lastReadMessageId);
}
