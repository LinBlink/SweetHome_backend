package asia.sweethome.api.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/2/2026 1:07 下午
 */
@Data
public class FamilyJoinInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String gender;
    private String inviteCode;
    private Long relationToMemberId;
    private String relationType;

}
