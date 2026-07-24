package asia.sweethome.chat.ws;

import lombok.Data;

/**
 * 【客户端→服务端的消息帧】
 * <p>
 * 客户端发来的所有 JSON 帧都反序列化成这个通用结构。哪些字段有意义取决于 type
 * （见 doc/api.md 5.2）：
 * <ul>
 *   <li>SEND_MESSAGE：用 conversationId + content + messageType + clientId；</li>
 *   <li>READ：用 conversationId + lastReadMessageId；</li>
 *   <li>PING / JOIN_CONVERSATION：只用 type。</li>
 * </ul>
 * 用「一个大结构装所有帧」而非每种帧一个类，是为了让 JSON 解析简单统一。
 */
@Data
public class InboundFrame {
    private String type;              // 帧类型（必填），决定其它字段如何解读
    private Long conversationId;      // 目标会话 id
    private String content;           // 消息内容（发消息时用）
    private String messageType;       // 消息类型 text/image/voice/system/redpacket
    private String clientId;          // 客户端生成的唯一 id，用于去重和乐观更新回显
    private Long lastReadMessageId;   // 已读到的消息 id（上报已读时用）
}
