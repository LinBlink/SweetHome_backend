package asia.sweethome.user.entity.vo;

import lombok.Data;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/1/2026 10:53 上午
 */
@Data
public class UserDetailVO {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String name;
    private String phone;
    private String avatarUrl;
    private Long familyId;
    private String familyName;
    private String role;
    private String gender;
}
