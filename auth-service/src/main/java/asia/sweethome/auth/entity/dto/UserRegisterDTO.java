package asia.sweethome.auth.entity.dto;

import lombok.Data;

/**
 * @description: 用户注册DTO
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:54 PM
 */
@Data
public class UserRegisterDTO {

    private String name;
    private String phone;
    private String password;
    private String familyName;
    private String inviteCode;

}
