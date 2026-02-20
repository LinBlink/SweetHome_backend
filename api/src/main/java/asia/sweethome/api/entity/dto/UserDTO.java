package asia.sweethome.api.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 【用户信息（跨服务返回值）】
 * <p>
 * user-service 查询用户后返回给调用方的数据，含用户本身 + 所在家庭的概要。
 * ⚠️ 注意 passwordHash 是敏感字段，仅供 auth-service 在登录时比对密码，
 * 绝不能直接透传给前端；返回前端时用的是不含密码的 UserInfoVO / UserDetailVO。
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 3:49 PM
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;                 // 用户 id
    private String phone;            // 手机号
    private String passwordHash;     // 加密后的密码（BCrypt 哈希），仅内部比对使用
    private String name;             // 昵称
    private String avatarUrl;        // 头像地址
    private String role;             // 在家庭中的角色，admin/member
    private Long familyId;           // 所在家庭 id，未加入家庭时为 null
    private String familyName;       // 所在家庭名称
    private LocalDateTime createdAt; // 注册时间
    private LocalDateTime updatedAt; // 最后更新时间

}
