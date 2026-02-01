package asia.sweethome.chat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 会话成员表（记录每个用户的已读进度）
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("conversation_members")
public class ConversationMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    private Long userId;

    private Long lastReadMessageId;

    private LocalDateTime lastReadAt;

    private LocalDateTime joinedAt;

    /**
     * 退出会话时间（NULL 表示仍在）
     */
    private LocalDateTime leftAt;

}
