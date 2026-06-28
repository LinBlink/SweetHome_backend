package asia.sweethome.health.entity.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

/**
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
@Data
public class HealthRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String metricType;

    private BigDecimal value;

    /**
     * 仅血压需要，舒张压
     */
    private BigDecimal valueSecondary;

    /**
     * 不传默认今天
     */
    private LocalDate recordedAt;

}
