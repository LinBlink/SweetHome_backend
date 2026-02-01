package asia.sweethome.family.entity.vo;

import lombok.Data;

@Data
public class FamilyMemberVO {
    private Long userId;
    private String name;
    private String gender;
    private String relationCode;
    private String relationLabel;
    private String avatarUrl;
    private Boolean isOnline;
    private String role;
}
