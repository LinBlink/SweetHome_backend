package asia.sweethome.auth.entity.vo.registerResponse;

import asia.sweethome.api.entity.vo.UserInfoVO;
import lombok.Data;

/**
 * 【注册成功的响应体】
 * <p>
 * 注册即登录：建号成功后同样返回双令牌 + 用户信息，前端拿到就是已登录状态，无需再登录一次。
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 9:24 PM
 */
@Data
public class RegisterResponseVO {

    private static final long serialVersionUID = 1L;

    private String token;         // accessToken
    private String refreshToken;  // refreshToken

    private UserInfoVO user;      // 新用户的基本信息

}
