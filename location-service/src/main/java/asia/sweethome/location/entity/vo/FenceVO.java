package asia.sweethome.location.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/14/2026 5:30 PM
 */

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FenceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Long setterUserId;

    private Long targetUserId;

    private Double fenceLng;

    private Double fenceLat;

    private Double fenceRange;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
