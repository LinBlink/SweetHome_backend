package asia.sweethome.user.entity.dto;

import lombok.Data;

/**
 * PUT /v1/users/me 请求体
 */
@Data
public class UserUpdateDTO {
    private String name;
    private String avatarUrl;
}
