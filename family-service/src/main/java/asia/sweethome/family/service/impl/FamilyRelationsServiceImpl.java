package asia.sweethome.family.service.impl;

import asia.sweethome.family.entity.po.FamilyRelation;
import asia.sweethome.family.mapper.FamilyRelationsMapper;
import asia.sweethome.family.service.IFamilyRelationsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 【家庭关系图 服务实现类】
 * <p>
 * 暂无自定义方法，完全复用 MyBatis-Plus 的通用 CRUD（save/removeByIds/lambdaQuery 等）。
 * 关系边的具体读写逻辑在 {@link FamiliesServiceImpl} 里调用这些通用方法完成。
 *
 * @author LocrianFifth
 * @since 2026-07-02
 */
@Service
public class FamilyRelationsServiceImpl extends ServiceImpl<FamilyRelationsMapper, FamilyRelation> implements IFamilyRelationsService {

}
