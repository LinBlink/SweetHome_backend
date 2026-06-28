package asia.sweethome.health.entity.dto;

import java.io.Serializable;
import java.time.LocalTime;

import lombok.Data;

/**
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
@Data
public class HealthReminderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalTime remindTime;

    private Boolean enabled;

}
