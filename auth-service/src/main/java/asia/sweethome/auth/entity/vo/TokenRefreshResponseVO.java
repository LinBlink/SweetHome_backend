package asia.sweethome.auth.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 10:22 PM
 */
@Data
@AllArgsConstructor
public class TokenRefreshResponseVO {

    private static final long serialVersionUID = 1L;

    String token;
}
