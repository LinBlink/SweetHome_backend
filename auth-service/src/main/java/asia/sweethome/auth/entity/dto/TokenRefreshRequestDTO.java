package asia.sweethome.auth.entity.dto;

import lombok.Data;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 10:23 PM
 */
@Data
public class TokenRefreshRequestDTO {

    private static final long serialVersionUID = 1L;

    String refreshToken;
}
