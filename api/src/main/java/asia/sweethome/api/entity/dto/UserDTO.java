package asia.sweethome.api.entity.dto;

import lombok.Data;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 3:49 PM
 */

@Data
public class UserDTO {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String phone;
    private String passwordHash;
    private String name;

}
