package asia.sweethome.family.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 【邀请码预览家庭（对外展示）】
 * <p>
 * GET /v1/families/lookup 的返回体：让还没加入的人看看这个家有哪些成员，
 * 以便选择「和谁建立什么关系」。
 */
@Data
public class FamilyLookupVO {
    private Long familyId;                       // 家庭 id
    private String familyName;                   // 家庭名称
    private List<FamilyLookupMemberVO> members;  // 成员简要列表
}
