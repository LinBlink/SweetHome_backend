package asia.sweethome.chat.service;

import asia.sweethome.chat.entity.po.ConversationMember;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 【会话成员 服务接口】继承通用 CRUD，补充成员关系与已读进度相关方法。
 */
public interface IConversationMembersService extends IService<ConversationMember> {

    // 是否为会话的活跃成员
    boolean isActiveMember(Long conversationId, Long userId);

    // 取活跃成员记录（没有返回 null）
    ConversationMember getActiveMember(Long conversationId, Long userId);

    // 列出会话的所有活跃成员
    List<ConversationMember> listActiveMembers(Long conversationId);

    // 列出某用户参与的所有活跃会话 id
    List<Long> listActiveConversationIds(Long userId);

    // 幂等：已是活跃成员则不重复插入；曾退出过（left_at 非空）则重新激活
    void addOrReactivate(Long conversationId, Long userId);

    // 幂等：非成员或已退出则什么都不做
    void leave(Long conversationId, Long userId);

    void markRead(Long conversationId, Long userId, Long lastReadMessageId);
}
