package asia.sweethome.chat.entity.dto;

import lombok.Data;

/**
 * 【创建单聊请求体】想和 targetUserId 开启一对一会话时的入参。
 */
@Data
public class CreateConversationDTO {
    private Long targetUserId;   // 想和谁单聊（对方用户 id）
}
