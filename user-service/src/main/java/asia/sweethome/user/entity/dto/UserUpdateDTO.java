package asia.sweethome.user.entity.dto;

import lombok.Data;

import java.time.LocalDate;

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
    // 新出生日期 YYYY-MM-DD（不传则不改）。它存在 family_members.birth_date 上（和 gender 一样
    // 是按家庭记录的），所以这里改的是「当前所在家庭」的那条成员记录。不得晚于今天。
    private LocalDate birthDate;
}
