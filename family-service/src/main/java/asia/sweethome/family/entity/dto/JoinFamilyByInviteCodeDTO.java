package asia.sweethome.family.entity.dto;

import lombok.Data;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/2/2026 10:49 上午
 */
@Data
public class JoinFamilyByInviteCodeDTO {
    private String inviteCode;
    private String gender;
    private Long relationToMemberId;
    private String relationType;
}
