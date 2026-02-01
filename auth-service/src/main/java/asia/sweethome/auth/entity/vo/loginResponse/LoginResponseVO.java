package asia.sweethome.auth.entity.vo.loginResponse;

import asia.sweethome.api.entity.vo.UserInfoVO;
import lombok.Data;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 9:24 PM
 */
@Data
public class LoginResponseVO {

    private static final long serialVersionUID = 1L;

    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";

    private UserInfoVO user;

}
