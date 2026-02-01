package asia.sweethome.family.service;

import asia.sweethome.family.entity.po.Family;
import asia.sweethome.family.entity.po.FamilyMemeber;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 家庭表 服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
public interface IFamiliesService extends IService<Family> {

    Long joinFamily(Long userId,
                    String inviteCode,
                    String gender,
                    Long relationToMemberId,
                    String relationType);

    Long createFamily(Long userId,
                      String gender,
                      String familyName);

    // 生成/复用邀请码，requesterUserId 必须是该家庭管理员
    Family generateInviteCode(Long familyId, Long requesterUserId);

    // 通过邀请码查找家庭（不存在或已过期抛 INVITE_CODE_INVALID）
    Family lookupByInviteCode(String inviteCode);

    List<FamilyMemeber> listActiveMembers(Long familyId);
}
