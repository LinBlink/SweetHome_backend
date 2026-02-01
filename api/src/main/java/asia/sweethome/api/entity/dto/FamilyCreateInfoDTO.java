package asia.sweethome.api.entity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/1/2026 11:05 下午
 */
@Data
public class FamilyCreateInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 谁创建的这个家
    private Long userId;
    // 家庭名字是什么
    private String familyName;
    // 创建者要加入家庭名单，需要知道性别
    private String gender;

}
