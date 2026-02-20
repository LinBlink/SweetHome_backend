package asia.sweethome.auth.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 【刷新令牌的响应体】
 * <p>
 * 续期成功后返回新的 accessToken。@AllArgsConstructor 让 new TokenRefreshResponseVO(token) 一行搞定。
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 10:22 PM
 */
@Data
@AllArgsConstructor
public class TokenRefreshResponseVO {

    private static final long serialVersionUID = 1L;

    String token;   // 新的 accessToken
}
