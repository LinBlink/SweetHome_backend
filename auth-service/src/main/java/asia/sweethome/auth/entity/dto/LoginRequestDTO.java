package asia.sweethome.auth.entity.dto;

import lombok.Data;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 9:23 PM
 */
@Data
public class LoginRequestDTO {

    private static final long serialVersionUID = 1L;

    private String phone;
    private String password;

}
