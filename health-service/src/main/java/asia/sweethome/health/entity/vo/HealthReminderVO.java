package asia.sweethome.health.entity.vo;

import java.io.Serializable;
import java.time.LocalTime;

import lombok.Data;

/**
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
@Data
public class HealthReminderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户从未设置过提醒时为 null
     */
    private LocalTime remindTime;

    private boolean enabled;

}
