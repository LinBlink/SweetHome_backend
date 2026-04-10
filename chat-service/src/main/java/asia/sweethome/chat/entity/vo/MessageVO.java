package asia.sweethome.chat.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 【消息（对外展示）】一条消息推给前端时的样子，除消息本身还带了发送者的展示信息
 * 和「接收者对发送者的称谓」（因人而异，由 ChatAssembler 按视角组装）。
 */
@Data
public class MessageVO {
    private Long id;                    // 消息 id
    private String clientId;           // 客户端 id（回显时前端据此把「发送中」替换成「已发送」）
    private Long conversationId;       // 所属会话
    private Long senderId;             // 发送者 id
    private String senderName;         // 发送者昵称
    private String senderAvatarLabel;  // 发送者头像文字
    private String senderRelationCode; // 我对发送者的关系编码；前端据此本地化为称谓
    private String content;            // 消息内容
    private String type;               // 消息类型 text/image/voice/system
    private LocalDateTime sentAt;      // 发送时间
}
