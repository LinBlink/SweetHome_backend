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
 * 
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("redpacket")
public class Redpacket implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 存储是谁发的红包
     */
    private Long userId;

    /**
     * 使用bigint存储金额方便计算，使用浮点会有精度损失
     */
    private Long totalAmount;

    /**
     * 红包个数
     */
    private Integer totalCount;

    /**
     * 红包剩余多少钱
     */
    private Long remainingAmount;

    /**
     * 红包剩余数量
     */
    private Integer remainingCount;

    /**
     * 冗余处理，方便以后根据家庭查找红包
     */
    private Long familyId;

    /**
     * 红包是哪个对话的
     */
    private Long conversationId;

    private String status;

    /**
     * 红包过期时间
     */
    private LocalDateTime expiredAt;

    /**
     * 红包创建时间
     */
    private LocalDateTime createdAt;


}
