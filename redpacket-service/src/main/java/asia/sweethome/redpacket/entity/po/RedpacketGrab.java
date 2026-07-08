package asia.sweethome.redpacket.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 记录每个红包每个人抢的情况，红包-用户：1-N
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("redpacket_grabs")
public class RedpacketGrab implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long redpacketId;

    private Long userId;

    private Long grabAmount;

    private LocalDateTime createdAt;


}
