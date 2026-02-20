package asia.sweethome.auth.entity.dto;

import lombok.Data;

/**
 * 【登录请求体】
 * <p>
 * 前端 POST /v1/auth/login 时的 JSON 会被自动映射成这个对象。
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 9:23 PM
 */
@Data
public class LoginRequestDTO {

    private static final long serialVersionUID = 1L;

    private String phone;    // 手机号（含国家码，如 +8613800138000）
    private String password; // 明文密码，后端会用 BCrypt 比对，不会明文存储

}
