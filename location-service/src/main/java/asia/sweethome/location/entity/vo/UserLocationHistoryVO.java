package asia.sweethome.location.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/14/2026 11:31 AM
 */

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class UserLocationHistoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long familyId;

    private String familyName;

    private Long userId;

    private String username;

    private String userAvatarUrl;

    private List<LocationPointVO> locations;

}
