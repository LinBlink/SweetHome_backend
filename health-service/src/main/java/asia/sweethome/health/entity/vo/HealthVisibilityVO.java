package asia.sweethome.health.entity.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
@Data
public class HealthVisibilityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String metricType;

    private boolean visible;

}
