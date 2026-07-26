package asia.sweethome.redpacket.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/23/2026 2:17 下午
 */

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RedpacketVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 存储是谁发的红包
     */
    private Long userId;

    private String username;

    private String userAvatarUrl;

    /**
     * 使用bigint存储金额方便计算，使用浮点会有精度损失
     */
    private Long totalAmount;

    /**
     * 红包个数
     */
    private Integer totalCount;


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
