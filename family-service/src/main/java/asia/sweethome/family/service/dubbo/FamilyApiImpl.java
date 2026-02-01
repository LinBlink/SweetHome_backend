package asia.sweethome.family.service.dubbo;

import asia.sweethome.api.FamilyApi;
import asia.sweethome.api.entity.dto.FamilyCreateInfoDTO;
import asia.sweethome.api.entity.dto.FamilyDTO;
import asia.sweethome.api.entity.dto.FamilyJoinInfoDTO;
import asia.sweethome.api.entity.dto.RelationDTO;
import asia.sweethome.api.entity.dto.RelationQueryDTO;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.family.entity.po.Family;
import asia.sweethome.family.entity.po.FamilyMemeber;
import asia.sweethome.family.entity.po.FamilyRelation;
import asia.sweethome.family.kinship.KinshipEngine;
import asia.sweethome.family.kinship.RelationResult;
import asia.sweethome.family.service.IFamiliesService;
import asia.sweethome.family.service.IFamilyMembersService;
import asia.sweethome.family.service.IFamilyRelationsService;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/1/2026 11:32 下午
 */
@DubboService
@RequiredArgsConstructor
public class FamilyApiImpl implements FamilyApi {
    private final IFamiliesService familiesService;
    private final IFamilyMembersService familyMembersService;
    private final IFamilyRelationsService familyRelationsService;
    private final KinshipEngine kinshipEngine;


    @Override
    public FamilyDTO createFamily(FamilyCreateInfoDTO familyCreateInfoDTO) {

        Long userId = familyCreateInfoDTO.getUserId();
        String familyName = familyCreateInfoDTO.getFamilyName();
        String gender = familyCreateInfoDTO.getGender();

        // 创建家庭
        Long familyId = familiesService.createFamily(userId, gender, familyName);

        // 构建返回值
        FamilyDTO familyDTO = new FamilyDTO();

        Family family = familiesService.getById(familyId);

        familyDTO.setName(family.getName());
        familyDTO.setId(family.getId());
        familyDTO.setInviteCode(family.getInviteCode());
        familyDTO.setInviteExpiresAt(family.getInviteExpiresAt());
        familyDTO.setCreatedBy(family.getCreatedBy());

        return familyDTO;


    }

    @Override
    public FamilyDTO joinFamily(FamilyJoinInfoDTO familyJoinInfoDTO) {

        Long userId = familyJoinInfoDTO.getUserId();
        String gender = familyJoinInfoDTO.getGender();
        String inviteCode = familyJoinInfoDTO.getInviteCode();
        Long relationToMemberId = familyJoinInfoDTO.getRelationToMemberId();
        String relationType = familyJoinInfoDTO.getRelationType();

        // 加入家庭
        Long familyId = familiesService.joinFamily(userId, inviteCode, gender, relationToMemberId, relationType);


        // 构建返回值
        FamilyDTO familyDTO = new FamilyDTO();

        Family family = familiesService.getById(familyId);

        familyDTO.setName(family.getName());
        familyDTO.setId(family.getId());
        familyDTO.setInviteCode(family.getInviteCode());
        familyDTO.setInviteExpiresAt(family.getInviteExpiresAt());
        familyDTO.setCreatedBy(family.getCreatedBy());

        return familyDTO;

    }

    // 根据用户 id 找到家庭信息
    @Override
    public FamilyDTO getFamilyByUserId(Long userId) {

        FamilyMemeber member = activeFamilyMemberByUserId(userId);

        if(  member == null ){
            throw new BusinessException(
                    ErrorCode.NO_SUCH_FAMILY_MEMBER
            );
        }

        Long familyId = member.getFamilyId();

        Family family = familiesService.getById(familyId);

        if( family == null ){
            throw new BusinessException(
                    ErrorCode.NO_SUCH_FAMILY
            );
        }

        return BeanUtil.copyProperties(family, FamilyDTO.class);


    }

    @Override
    public String getFamilyRoleByUserId(Long userId) {
        FamilyMemeber member = activeFamilyMemberByUserId(userId);
        return member == null ? null : member.getRole();
    }

    @Override
    public String getGenderByUserId(Long userId) {
        FamilyMemeber member = activeFamilyMemberByUserId(userId);
        return member == null ? null : member.getGender();
    }

    @Override
    public RelationDTO getRelation(RelationQueryDTO relationQueryDTO) {

        Long viewerUserId = relationQueryDTO.getViewerUserId();
        Long targetUserId = relationQueryDTO.getTargetUserId();

        FamilyMemeber viewerMember = activeFamilyMemberByUserId(viewerUserId);
        FamilyMemeber targetMember = activeFamilyMemberByUserId(targetUserId);

        if (viewerMember == null || targetMember == null
                || !viewerMember.getFamilyId().equals(targetMember.getFamilyId())) {
            RelationResult none = RelationResult.NONE;
            return new RelationDTO(none.relationCode(), none.relationLabel());
        }

        Long familyId = viewerMember.getFamilyId();

        List<FamilyRelation> relations = familyRelationsService.lambdaQuery()
                .eq(FamilyRelation::getFamilyId, familyId)
                .isNull(FamilyRelation::getDeletedAt)
                .list();

        Map<Long, FamilyMemeber> membersById = familyMembersService.lambdaQuery()
                .eq(FamilyMemeber::getFamilyId, familyId)
                .isNull(FamilyMemeber::getDeletedAt)
                .list()
                .stream()
                .collect(Collectors.toMap(FamilyMemeber::getId, m -> m));

        RelationResult result = kinshipEngine.computeRelation(
                relations, membersById, viewerMember.getId(), targetMember.getId(),
                relationQueryDTO.getAcceptLanguage()
        );

        return new RelationDTO(result.relationCode(), result.relationLabel());
    }

    private FamilyMemeber activeFamilyMemberByUserId(Long userId) {
        return familyMembersService.lambdaQuery()
                .eq(FamilyMemeber::getUserId, userId)
                .isNull(FamilyMemeber::getDeletedAt)
                .one();
    }
}
