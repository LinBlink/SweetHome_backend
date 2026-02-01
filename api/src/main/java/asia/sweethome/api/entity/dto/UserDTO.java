package asia.sweethome.api.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 3:49 PM
 */

@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String phone;
    private String passwordHash;
    private String name;
    private String avatarUrl;
    private String role;
    private Long familyId;
    private String familyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
