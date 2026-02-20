package asia.sweethome.api.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 【加入家庭的入参】
 * <p>
 * 通过 Dubbo 传给 family-service 的 joinFamily 方法。除了「谁、用哪个邀请码加入」，
 * 还带上「我和家里某位成员是什么关系」，以便一进门就建立亲属关系网。
 *
 * @author: LOCRIAN_V
 * @date: 7/2/2026 1:07 下午
 */
@Data
public class FamilyJoinInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;              // 要加入的用户 id
    private String gender;            // 该用户性别（计算亲属称谓用）
    private String inviteCode;        // 家庭邀请码，凭它找到目标家庭
    private Long relationToMemberId;  // 「我」和家里哪位成员建立关系（对方的 userId）
    private String relationType;      // 关系类型，取值见 RelationTypeConstants，如 CHILD_OF

}
