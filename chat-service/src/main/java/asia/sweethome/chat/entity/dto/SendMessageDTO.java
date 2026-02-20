package asia.sweethome.chat.entity.dto;

import lombok.Data;

/**
 * 【发消息请求体】用于（可能的）REST 发消息接口；WebSocket 发消息走 InboundFrame。
 */
@Data
public class SendMessageDTO {
    private String content;   // 消息内容
    private String type;      // 消息类型
    private String clientId;  // 客户端唯一 id（去重用）
}
