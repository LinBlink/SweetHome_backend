package asia.sweethome.health.entity.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

/**
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
@Data
public class HealthRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String metricType;

    private BigDecimal value;

    private BigDecimal valueSecondary;

    private LocalDate recordedAt;

}
