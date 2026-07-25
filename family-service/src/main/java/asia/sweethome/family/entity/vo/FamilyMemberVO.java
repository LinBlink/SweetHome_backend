package asia.sweethome.family.entity.vo;


import java.time.LocalDate;
import lombok.Data;

/**
 * 【家庭成员（对外展示）】
 * <p>
 * GET /v1/families/{familyId}/members 列表里的一项，聚合了成员基础信息、昵称头像、
 * 在线状态，以及「当前登录者对该成员」的亲属称谓。
 */
@Data
public class FamilyMemberVO {
    private Long userId;          // 成员的用户 id
    private Long memberId;        // 成员的 memberId
    private String name;          // 昵称
    private String gender;        // 性别
    // 出生日期 YYYY-MM-DD，未填时为 null。客户端据此在族谱上显示年龄和生日；
    // 年龄不由服务端算——它每天都会变，算好了塞进响应里就会随缓存变旧，客户端按当天算更准。
    private LocalDate birthDate;
    private String relationCode;  // 关系编码，如 F.F；前端据此本地化为称谓
    private String avatarUrl;     // 头像
    private Boolean isOnline;     // 是否在线（来自 chat-service）
    private String role;          // 在家庭中的角色 admin/member
}
