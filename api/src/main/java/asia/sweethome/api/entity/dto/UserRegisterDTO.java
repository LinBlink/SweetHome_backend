package asia.sweethome.api.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 用户注册DTO
 * @author: LOCRIAN_V
 * @date: 6/29/2026 10:54 PM
 */
@Data
public class UserRegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String phone;
    private String password;
    private String familyName;
    private String inviteCode;
    private String gender;
    private Long relationToMemberId;
    private String relationType;

}
