package asia.sweethome.family.service;

import asia.sweethome.family.entity.po.FamilyMemeber;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 家庭成员关系表 服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
public interface IFamilyMembersService extends IService<FamilyMemeber> {

    Integer getFamilyMemberCount(Long familyId);
}
