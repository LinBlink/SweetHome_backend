package asia.sweethome.family.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/2/2026 10:38 上午
 */
@Data
public class FamilyDetailVO {

    private Long familyId;
    private String name;
    private Integer memberCount;
    private LocalDateTime createdAt;

}
