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
 * 家庭成员关系图（血亲 PARENT_OF 有向边 + 姻亲 SPOUSE_OF 无向边）
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("family_relations")
public class FamilyRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 家庭 ID（冗余字段，便于按家庭批量取整张关系图）
     */
    private Long familyId;

    /**
     * 关系主体 family_members.id
     */
    private Long subjectMemberId;

    /**
     * PARENT_OF=subject 是 object 的父/母（有向）；SPOUSE_OF=互为配偶（无方向，写入时需规范化 subject_member_id < object_member_id 以防重复行）
     */
    private String relationType;

    /**
     * 关系客体 family_members.id
     */
    private Long objectMemberId;

    private LocalDateTime createdAt;

    /**
     * 软删除时间戳（如离婚、关系录入错误撤销）
     */
    private LocalDateTime deletedAt;


}
