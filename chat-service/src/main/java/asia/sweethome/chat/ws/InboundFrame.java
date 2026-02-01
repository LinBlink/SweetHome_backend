package asia.sweethome.chat.ws;

import lombok.Data;

/**
 * WebSocket 客户端 -&gt; 服务端帧的通用形状，字段是否有意义取决于 type（见 doc/api.md 5.2）：
 * SEND_MESSAGE / JOIN_CONVERSATION / READ / PING
 */
@Data
public class InboundFrame {
    private String type;
    private Long conversationId;
    private String content;
    private String messageType;
    private String clientId;
    private Long lastReadMessageId;
}
