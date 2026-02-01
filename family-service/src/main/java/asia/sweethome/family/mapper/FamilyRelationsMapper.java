package asia.sweethome.family.mapper;

import asia.sweethome.family.entity.po.FamilyRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 家庭成员关系图（血亲 PARENT_OF 有向边 + 姻亲 SPOUSE_OF 无向边） Mapper 接口
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-02
 */
public interface FamilyRelationsMapper extends BaseMapper<FamilyRelation> {

}
