package asia.sweethome.auth.entity.vo.loginResponse;

import asia.sweethome.api.entity.vo.UserInfoVO;
import lombok.Data;

/**
 * 【登录成功的响应体】
 * <p>
 * 一次性把「两把令牌 + 用户信息」返回给前端。前端保存令牌后，后续请求在
 * 请求头里带上 {@code Authorization: Bearer <token>} 即可证明身份。
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 9:24 PM
 */
@Data
public class LoginResponseVO {

    private static final long serialVersionUID = 1L;

    private String token;                 // accessToken（短期，日常请求带它）
    private String refreshToken;          // refreshToken（长期，过期后用来换新的 accessToken）
    private String tokenType = "Bearer";  // 令牌类型，固定 Bearer，是 HTTP 标准的携带方式

    private UserInfoVO user;              // 用户基本信息（不含密码）

}
