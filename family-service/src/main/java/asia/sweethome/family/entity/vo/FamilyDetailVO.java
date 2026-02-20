package asia.sweethome.family.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 【家庭详情（对外展示）】家庭主页顶部展示的概要信息。
 *
 * @author: LOCRIAN_V
 * @date: 7/2/2026 10:38 上午
 */
@Data
public class FamilyDetailVO {

    private Long familyId;          // 家庭 id
    private String name;            // 家庭名称
    private Integer memberCount;    // 成员数量
    private LocalDateTime createdAt;// 家庭创建时间

}
