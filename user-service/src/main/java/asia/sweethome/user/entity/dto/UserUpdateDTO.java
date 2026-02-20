package asia.sweethome.user.entity.dto;

import lombok.Data;

/**
 * 【更新个人资料请求体】
 * <p>
 * PUT /v1/users/me 请求体。两个字段都是「可选」：只传想改的即可，
 * 不传（null）的字段服务端会保持原值（部分更新，见 UsersServiceImpl.updateProfile）。
 */
@Data
public class UserUpdateDTO {
    private String name;       // 新昵称（不传则不改）
    private String avatarUrl;  // 新头像地址（不传则不改）
}
