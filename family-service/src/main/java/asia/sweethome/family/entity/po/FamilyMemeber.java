package asia.sweethome.family.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 家庭成员关系表
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("family_members")
public class FamilyMemeber implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 家庭 ID
     */
    private Long familyId;

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
