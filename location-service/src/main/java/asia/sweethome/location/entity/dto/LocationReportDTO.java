package asia.sweethome.location.entity.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/13/2026 4:01 PM
 */
@Data
public class LocationReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double lng;

    private Double lat;

    private Integer battery;

    private LocalDateTime updateTime;

}
