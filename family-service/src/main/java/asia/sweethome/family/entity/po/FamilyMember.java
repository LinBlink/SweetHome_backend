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
 * 【family_members 表实体（PO）】
 * <p>
 * 表示「某用户是某家庭的成员」，一条记录 = 一个成员身份。它是关系图上的「节点」，
 * 亲属关系（边）则存在 family_relations（{@link FamilyRelation}）。
 * <p>
 * 注意：类名 FamilyMember 是拼写笔误（应为 FamilyMember），因已被各处引用，暂保持原样不改名。
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("family_members")
public class FamilyMember implements Serializable {

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
