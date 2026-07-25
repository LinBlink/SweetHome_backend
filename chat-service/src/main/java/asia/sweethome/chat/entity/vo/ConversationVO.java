package asia.sweethome.chat.entity.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 【会话（对外展示）】会话列表里的一项，字段基本对应聊天列表页每一行要显示的内容。
 */
@Data
public class ConversationVO {
    private Long id;                    // 会话 id
    private String type;               // group 群聊 / direct 单聊
    private String name;               // 显示名（群聊=群名；单聊=对方昵称）
    private Long familyId;             // 所属家庭 id
    private String avatarLabel;        // 头像文字
    private String avatarColor;        // 头像颜色
    private String avatarUrl;          // 头像URL
    // 仅 type=direct 时返回：对方相对当前请求用户的关系编码（前端据此本地化为称谓）
    private String relationCode;
    // 仅 type=direct 时返回：对方性别 male/female。前端拿它消歧 relationCode="S"（丈夫/妻子）。
    // 「我」自己的性别不用在这里重复下发——GET /v1/users/me 已经返回 gender，前端从登录态取
    // （消歧 S.F=岳父/公公 这类随我方性别而变的称谓时用）。
    private String otherUserGender;
    private String lastMessage;        // 最后一条消息预览
    private LocalDateTime lastMessageAt;// 最后消息时间（列表排序用）
    private String lastMessageType;
    private Long unreadCount;          // 我的未读数
    private Integer memberCount;       // 成员数
}
