package asia.sweethome.chat.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {
    private Long id;
    private String type;
    private String name;
    private Long familyId;
    private String avatarLabel;
    private String avatarColor;
    // 仅 type=direct 时返回：对方相对当前请求用户的称谓
    private String relationCode;
    private String relationLabel;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    private Integer memberCount;
}
