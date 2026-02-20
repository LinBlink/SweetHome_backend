package asia.sweethome.family.service;

import asia.sweethome.family.entity.po.FamilyMemeber;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 【家庭成员 服务接口】继承通用 CRUD，另加「统计家庭人数」。
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
public interface IFamilyMembersService extends IService<FamilyMemeber> {

    // 统计某家庭的在册成员数
    Integer getFamilyMemberCount(Long familyId);
}
