package asia.sweethome.location.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/13/2026 6:06 PM
 */

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class FamilyMemberLocationsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long familyId;

    private String familyName;

    private Integer onlineMemberCount;

    private Integer totalMemberCount;

    private List<FamilyMemberLocationVO> familyMemberLocations;


}
