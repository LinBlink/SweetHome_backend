package asia.sweethome.chat.entity.dto;

import lombok.Data;

/**
 * 【标记已读请求体】上报「我已读到 lastReadMessageId 这条」，服务端据此更新未读数。
 */
@Data
public class MarkReadDTO {
    private Long lastReadMessageId;   // 已读到的最后一条消息 id
}
