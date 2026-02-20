package asia.sweethome.auth.entity.dto;

import lombok.Data;

/**
 * 【刷新令牌 / 登出的请求体】
 * <p>
 * 刷新 accessToken 和登出，都只需要前端把 refreshToken 传上来，所以共用这一个请求体。
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 10:23 PM
 */
@Data
public class TokenRefreshRequestDTO {

    private static final long serialVersionUID = 1L;

    String refreshToken;   // 登录时下发的长期令牌
}
