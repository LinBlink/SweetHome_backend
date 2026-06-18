package asia.sweethome.location.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("outbox_messages")
public class OutboxMessage implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String topic;

    private String payload;


    public static final boolean STATUS_SENT = true;
    public static final boolean STATUS_UNSEND = false;
    private Boolean status;

    private Integer retryCount;

    private LocalDateTime createdAt;


}
