package asia.sweethome.location.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/14/2026 11:40 AM
 */

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LocationPointVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double lng;

    private Double lat;

    private Integer battery;

    private LocalDateTime updatedAt;

}
