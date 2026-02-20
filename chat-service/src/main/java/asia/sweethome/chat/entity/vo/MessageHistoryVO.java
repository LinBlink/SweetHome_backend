package asia.sweethome.chat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 【历史消息分页结果（对外展示）】
 * <p>
 * hasMore=是否还有更早的消息（用于前端「加载更多」按钮）；
 * nextCursor=下一页游标（取更早消息时把它当作 before 传回来，见 MessagesServiceImpl.listPage）。
 */
@Data
@AllArgsConstructor
public class MessageHistoryVO {
    private List<MessageVO> messages;  // 本页消息
    private boolean hasMore;           // 是否还有更早的
    private Long nextCursor;           // 下一页游标
}
