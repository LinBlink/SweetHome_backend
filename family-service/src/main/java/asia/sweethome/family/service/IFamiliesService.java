package asia.sweethome.family.service;

import asia.sweethome.family.entity.po.Family;
import asia.sweethome.family.entity.po.FamilyMember;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 【家庭 服务接口】
 * <p>
 * 继承 MyBatis-Plus 的 IService 获得通用 CRUD；这里声明家庭相关的业务方法。
 * 实现见 {@link asia.sweethome.family.service.impl.FamiliesServiceImpl}。
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
public interface IFamiliesService extends IService<Family> {

    // 加入家庭（含退出旧家庭级联、补关系边、加群聊），返回家庭 id
    Long joinFamily(Long userId,
                    String inviteCode,
                    String gender,
                    Long relationToMemberId,
                    String relationType);

    // 创建家庭（创建者成为管理员并自动建群聊），返回家庭 id
    Long createFamily(Long userId,
                      String gender,
                      String familyName);

    // 生成/复用邀请码，requesterUserId 必须是该家庭管理员
    Family generateInviteCode(Long familyId, Long requesterUserId);

    // 通过邀请码查找家庭（不存在或已过期抛 INVITE_CODE_INVALID）
    Family lookupByInviteCode(String inviteCode);

    List<FamilyMember> listActiveMembers(Long familyId);
}
