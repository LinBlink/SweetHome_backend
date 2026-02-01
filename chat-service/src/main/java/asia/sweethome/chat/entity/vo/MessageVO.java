package asia.sweethome.chat.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Long id;
    private String clientId;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderAvatarLabel;
    private String senderRelationCode;
    private String senderRelationLabel;
    private String content;
    private String type;
    private LocalDateTime sentAt;
}
