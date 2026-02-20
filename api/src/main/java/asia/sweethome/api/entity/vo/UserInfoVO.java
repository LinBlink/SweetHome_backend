package asia.sweethome.api.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 【用户基本信息（对外展示）】
 * <p>
 * VO = View Object，是「给前端看」的对象。相比 {@link asia.sweethome.api.entity.dto.UserDTO}，
 * 这里刻意不含 passwordHash 等敏感字段，注册成功后返回给前端展示。
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 9:32 PM
 */
@Data
public class UserInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;       // 用户 id
    private String name;       // 昵称
    private String phone;      // 手机号
    private Long familyId;     // 所在家庭 id
    private String familyName; // 所在家庭名称
    private String role;       // 在家庭中的角色
}
