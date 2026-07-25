package asia.sweethome.family.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
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
     * 出生日期，用于判定同辈之间的长幼（哥/弟、姐/妹）。
     * <p>
     * 比 {@link #birthOrder} 可靠得多：birthOrder 要用户手填「我在兄弟姐妹里排第几」，
     * 而注册/加入家庭的流程里压根没有这一步，所以它几乎总是 null——结果是
     * {@code siblingToken} 一直走「排行未知默认按年长」的兜底分支，线上几乎所有兄弟姐妹
     * 都被叫成了「哥/姐」。生日只需要用户填一个本来就会填的字段，不用理解「排行」的语义。
     * <p>
     * 判定时优先用它（日期早的年长），两边都有值才算得准；缺失时才退回 birthOrder。
     */
    private LocalDate birthDate;

    /**
     * 出生顺序，数值越小越年长。仅作为 {@link #birthDate} 缺失时的兜底。
     * <p>
     * ⚠️ 语义约定（原先没写清，容易录成两种含义）：这是「<b>同胞</b>之间的排行」，
     * 即在同一对父母的孩子里排第几，而不是「家庭内的全局序号」。两种理解在多数场景下
     * 碰巧结果一样（引擎只在同辈之间比较），一旦录入方式不统一就会算错长幼。
     */
    private Integer birthOrder;


}
