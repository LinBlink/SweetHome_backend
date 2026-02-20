package asia.sweethome.family.entity.vo;

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
    private String name;          // 昵称
    private String gender;        // 性别
    private String relationCode;  // 关系编码（程序用），如 F.F
    private String relationLabel; // 关系称谓（给人看），如 Paternal Grandfather
    private String avatarUrl;     // 头像
    private Boolean isOnline;     // 是否在线（来自 chat-service）
    private String role;          // 在家庭中的角色 admin/member
}
