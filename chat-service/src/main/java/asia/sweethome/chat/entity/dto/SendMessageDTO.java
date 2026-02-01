package asia.sweethome.chat.entity.dto;

import lombok.Data;

@Data
public class SendMessageDTO {
    private String content;
    private String type;
    private String clientId;
}
