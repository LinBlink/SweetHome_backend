package asia.sweethome.common.entity.ko;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/17/2026 1:20 下午
 */

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FenceAlarmMessageKO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long fenceId;

    private String fenceName;


    public static final String ALARM_TYPE_STEPPED_OUTSIDE = "STEPPED_OUTSIDE";

    public static final String ALARM_TYPE_STEPPED_INSIDE = "STEPPED_INSIDE";


    private String alarmType;

    private LocalDateTime alarmedAt;

    private Long setterUserId;

    private Long targetUserId;

    private Long familyId;

}