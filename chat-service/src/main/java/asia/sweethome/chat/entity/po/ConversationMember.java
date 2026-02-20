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
 * 【conversation_members 表实体（PO）】记录「某用户在某会话里」的成员关系与已读进度。
 * lastReadMessageId 记住读到哪了，用来算未读数；leftAt 非空表示已退出（软退出）。
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
