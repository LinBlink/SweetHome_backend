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
public class RedpacketGrabVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long id;

    private Long redpacketId;

    private Long grabAmount;

    private LocalDateTime createdAt;

}