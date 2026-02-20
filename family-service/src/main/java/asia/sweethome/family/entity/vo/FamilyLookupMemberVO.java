package asia.sweethome.family.entity.vo;

import lombok.Data;

/**
 * 【预览家庭时的单个成员信息】
 * <p>
 * 注意这里给的是 memberId（family_members.id，即关系图节点 id），
 * 因为加入时的 relationToMemberId 需要的正是这个 id。
 */
@Data
public class FamilyLookupMemberVO {
    private Long memberId;    // 成员 id（family_members.id）
    private String name;      // 昵称（来自 user-service）
    private String gender;    // 性别
    private String avatarUrl; // 头像（来自 user-service）
}
