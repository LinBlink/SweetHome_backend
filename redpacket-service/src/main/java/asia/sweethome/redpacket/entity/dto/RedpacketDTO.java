package asia.sweethome.redpacket.entity.dto;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/21/2026 6:39 下午
 */

import java.io.Serializable;

import lombok.Data;

@Data
public class RedpacketDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 使用bigint存储金额方便计算，使用浮点会有精度损失
     */
    private Long totalAmount;

    /**
     * 红包个数
     */
    private Integer totalCount;

    /**
     * 红包是哪个对话的
     */
    private Long conversationId;


}