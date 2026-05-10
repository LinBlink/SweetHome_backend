package asia.sweethome.location.entity.ro;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/13/2026 5:38 PM
 */
@Data
public class CurrentLocationRO implements Serializable {

    private static final Long serialVersionUID = 1L;

    private Long userId;

    private Long familyId;

    private Double lng;

    private Double lat;

    private Integer battery;

    private LocalDateTime updatedAt;

}
