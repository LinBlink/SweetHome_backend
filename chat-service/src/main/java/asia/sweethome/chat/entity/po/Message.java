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
 * 【messages 表实体（PO）】一条聊天消息。
 * clientId 是客户端生成的唯一标识，用于「乐观更新」（发出去先本地显示）和「去重」（网络重发不存两条）；
 * replyToId 指向被引用回复的消息 id。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("messages")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    private Long senderId;

    /**
     * text / image / voice / system
     */
    private String type;

    private String content;

    /**
     * 客户端 UUID，用于乐观更新 echo 和重复投递去重
     */
    private String clientId;

    private Long replyToId;

    private LocalDateTime sentAt;

    private LocalDateTime deletedAt;

}
