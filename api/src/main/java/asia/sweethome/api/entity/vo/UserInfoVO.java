package asia.sweethome.api.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 9:32 PM
 */

@Data
public class UserInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String name;
    private String phone;
    private Long familyId;
    private String familyName;
    private String role;
}
