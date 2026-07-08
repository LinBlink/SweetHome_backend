package asia.sweethome.api;

import java.util.List;

/**
 * 【chat-service 对外暴露的 Dubbo 接口】
 * <p>
 * 主要给 family-service 调用：在家庭创建/加入/退出时，联动维护家庭群聊会话
 * （conversations 表，type=group）。也提供「筛在线用户」给成员列表用。
 * （关于 Dubbo 接口的原理，见 {@link FamilyApi} 的说明。）
 *
 * @author: LOCRIAN_V
 */
public interface ChatApi {

    // 创建家庭群聊会话，返回 conversation id；memberUserIds 为初始成员
    Long createGroupConversation(Long familyId, String name, List<Long> memberUserIds);

    // 将用户加入家庭群聊（conversation_members），幂等（已在群里不会重复加）
    void addMemberToGroupConversation(Long familyId, Long userId);

    // 用户退出家庭群聊（conversation_members.left_at = now），幂等
    void removeMemberFromGroupConversation(Long familyId, Long userId);

    // 从给定 userId 列表中筛选出当前在线（任意 chat-service 实例持有活跃 WebSocket 连接）的用户
    List<Long> filterOnlineUserIds(List<Long> userIds);

    // 得到对话中的成员数量
    Long getConversationMemberCount(Long conversation);

    // 判断某个用户是否是对话中的成员
    Boolean userExistsInConversation(Long userId, Long conversationId);

    Long getConversationFamilyId(Long conversationId);

}
