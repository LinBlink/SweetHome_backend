package asia.sweethome.chat.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 【conversations 表实体（PO）】一次「会话」= 一个聊天窗口（群聊或单聊）。
 * lastMessageId / lastMessageAt 是冗余字段，用于会话列表快速展示「最后一条消息」和排序，
 * 避免每次都去 messages 表现算。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("conversations")
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * group / direct
     */
    private String type;

    /**
     * 群聊名称；私聊为 NULL
     */
    private String name;

    /**
     * 所属家庭 ID；跨家庭私聊可为 NULL
     */
    private Long familyId;

    private Long lastMessageId;

    private LocalDateTime lastMessageAt;

    private String lastMessageType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

}
