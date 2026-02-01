package asia.sweethome.api;

import java.util.List;

/**
 * chat-service 暴露给其他服务（主要是 family-service）的 Dubbo 接口，
 * 用于在家庭创建/加入/退出时联动维护家庭群聊会话（conversations, type=group）。
 *
 * @description:
 * @author: LOCRIAN_V
 */
public interface ChatApi {

    // 创建家庭群聊会话，返回 conversation id；memberUserIds 为初始成员
    Long createGroupConversation(Long familyId, String name, List<Long> memberUserIds);

    // 将用户加入家庭群聊（conversation_members），幂等
    void addMemberToGroupConversation(Long familyId, Long userId);

    // 用户退出家庭群聊（conversation_members.left_at = now），幂等
    void removeMemberFromGroupConversation(Long familyId, Long userId);

    // 从给定 userId 列表中筛选出当前在线（本进程或其它 chat-service 实例持有活跃 WebSocket 连接）的用户
    List<Long> filterOnlineUserIds(List<Long> userIds);
}
