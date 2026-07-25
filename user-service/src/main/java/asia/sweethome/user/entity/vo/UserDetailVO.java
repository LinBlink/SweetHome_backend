package asia.sweethome.user.entity.vo;


import java.time.LocalDate;
import lombok.Data;

/**
 * 【用户详情（对外展示）】
 * <p>
 * GET /v1/users/me 的返回体，聚合了「用户本身 + 所在家庭」的信息，供个人中心页面展示。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 10:53 上午
 */
@Data
public class UserDetailVO {

    private static final long serialVersionUID = 1L;

    private Long userId;       // 用户 id
    private String name;       // 昵称
    private String phone;      // 手机号
    private String avatarUrl;  // 头像地址
    private Long familyId;     // 所在家庭 id
    private String familyName; // 所在家庭名称
    private String role;       // 在家庭中的角色 admin/member
    private String gender;     // 性别 male/female
    private LocalDate birthDate; // 出生日期 YYYY-MM-DD，未填时为 null（存在 family_members 上）
    private Long balance;      // 用户余额
}
