package asia.sweethome.api;

import asia.sweethome.api.entity.dto.FamilyCreateInfoDTO;
import asia.sweethome.api.entity.dto.FamilyDTO;
import asia.sweethome.api.entity.dto.FamilyJoinInfoDTO;
import asia.sweethome.api.entity.dto.RelationDTO;
import asia.sweethome.api.entity.dto.RelationQueryDTO;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/1/2026 11:01 下午
 */

public interface FamilyApi {
    FamilyDTO createFamily(
            FamilyCreateInfoDTO familyCreateInfoDTO
    );

    FamilyDTO joinFamily(
            FamilyJoinInfoDTO familyJoinInfoDTO
    );

    FamilyDTO getFamilyByUserId(
            Long userId
    );

    // 根据用户 id 找到其在家庭中的角色
    String getFamilyRoleByUserId(
            Long userId
    );

    // 根据用户 id 找到其在当前家庭中的性别（写入 family_members.gender，亲属称谓计算的基础输入）
    String getGenderByUserId(
            Long userId
    );

    // 计算 viewer 相对 target 的亲属称谓，两人不在同一家庭时返回的 DTO 内字段均为 null
    RelationDTO getRelation(
            RelationQueryDTO relationQueryDTO
    );
}
