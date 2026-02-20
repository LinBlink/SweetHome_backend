package asia.sweethome.family.service.impl;

import asia.sweethome.family.entity.po.FamilyMemeber;
import asia.sweethome.family.mapper.FamilyMembersMapper;
import asia.sweethome.family.service.IFamilyMembersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 【家庭成员 服务实现类】
 * <p>
 * 继承 MyBatis-Plus 的 ServiceImpl，白得通用 CRUD；这里补一个「统计家庭人数」的方法。
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
@Service
public class FamilyMembersServiceImpl extends ServiceImpl<FamilyMembersMapper, FamilyMemeber> implements IFamilyMembersService {

    /**
     * 统计某家庭的在册成员数。
     * count() 返回 long，这里用 Math.toIntExact 转成 int（若真超过 int 上限会抛异常提示，比强转截断更安全）。
     * 注：deletedAt 是全局逻辑删除字段，count 会自动排除已退出成员。
     */
    @Override
    public Integer getFamilyMemberCount(Long familyId) {
        return Math.toIntExact(lambdaQuery().eq(
                FamilyMemeber::getFamilyId, familyId
        ).count());
    }
}
