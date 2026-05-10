package asia.sweethome.api.entity.dto;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/13/2026 6:42 PM
 */

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FamilyMemberDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 家庭角色：admin=管理员，member=成员
     */
    private String role;

    /**
     * 加入时间
     */
    private LocalDateTime joinedAt;

    /**
     * 退出/被移除时间（软删除）
     */
    private LocalDateTime deletedAt;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生顺序
     */
    private Integer birthOrder;


}
