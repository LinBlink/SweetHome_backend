package asia.sweethome.location.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/14/2026 6:10 PM
 */

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FenceAlarmVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long fenceId;

    /**
     * 围栏可能已经被设置者删除，删除后这里为 null
     */
    private String fenceName;

    private String alarmType;

    private LocalDateTime alarmedAt;

    private Long targetUserId;

    private String targetUsername;

    private String targetUserAvatarUrl;

}
