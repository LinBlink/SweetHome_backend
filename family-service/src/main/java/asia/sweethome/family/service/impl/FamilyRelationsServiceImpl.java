package asia.sweethome.family.service.impl;

import asia.sweethome.family.entity.po.FamilyRelation;
import asia.sweethome.family.mapper.FamilyRelationsMapper;
import asia.sweethome.family.service.IFamilyRelationsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 家庭成员关系图（血亲 PARENT_OF 有向边 + 姻亲 SPOUSE_OF 无向边） 服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-02
 */
@Service
public class FamilyRelationsServiceImpl extends ServiceImpl<FamilyRelationsMapper, FamilyRelation> implements IFamilyRelationsService {

}
