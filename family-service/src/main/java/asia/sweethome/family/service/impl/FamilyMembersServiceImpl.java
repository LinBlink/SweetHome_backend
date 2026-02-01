package asia.sweethome.family.service.impl;

import asia.sweethome.family.entity.po.FamilyMemeber;
import asia.sweethome.family.mapper.FamilyMembersMapper;
import asia.sweethome.family.service.IFamilyMembersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 家庭成员关系表 服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
@Service
public class FamilyMembersServiceImpl extends ServiceImpl<FamilyMembersMapper, FamilyMemeber> implements IFamilyMembersService {

    @Override
    public Integer getFamilyMemberCount(Long familyId) {
        return Math.toIntExact(lambdaQuery().eq(
                FamilyMemeber::getFamilyId, familyId
        ).count());
    }
}
