package asia.sweethome.family.service;

import asia.sweethome.family.entity.po.FamilyRelation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 家庭成员关系图（血亲 PARENT_OF 有向边 + 姻亲 SPOUSE_OF 无向边） 服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-02
 */
public interface IFamilyRelationsService extends IService<FamilyRelation> {

}
