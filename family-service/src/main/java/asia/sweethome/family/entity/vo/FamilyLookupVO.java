package asia.sweethome.family.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class FamilyLookupVO {
    private Long familyId;
    private String familyName;
    private List<FamilyLookupMemberVO> members;
}
