package asia.sweethome.location.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/13/2026 6:12 PM
 */

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FamilyMemberLocationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private String userAvatarUrl;

    private Double lng;

    private Double lat;

    private Integer battery;

    private LocalDateTime updatedAt;

}