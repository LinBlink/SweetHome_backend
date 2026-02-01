package asia.sweethome.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MessageHistoryVO {
    private List<MessageVO> messages;
    private boolean hasMore;
    private Long nextCursor;
}
