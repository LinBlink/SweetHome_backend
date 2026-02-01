package asia.sweethome.family.entity.vo;

import lombok.Data;

@Data
public class FamilyLookupMemberVO {
    private Long memberId;
    private String name;
    private String gender;
    private String avatarUrl;
}
