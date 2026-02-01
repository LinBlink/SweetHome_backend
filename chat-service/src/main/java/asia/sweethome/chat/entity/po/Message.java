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
 * 消息表
 * </p>
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
