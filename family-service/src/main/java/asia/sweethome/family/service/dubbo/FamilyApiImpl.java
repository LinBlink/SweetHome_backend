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
import asia.sweethome.family.entity.po.FamilyMember;
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
 * 【FamilyApi 的 Dubbo 实现】
 * <p>
 * family-service 对外（主要给 user-service、auth-service）提供的远程接口实现。
 * 大多是「查某用户在家庭里的信息」以及「注册时联动建/入家庭」，具体家庭业务委托给
 * {@link IFamiliesService} 等本服务的 Service 完成。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 11:32 下午
 */
@DubboService
@RequiredArgsConstructor
public class FamilyApiImpl implements FamilyApi {
    private final IFamiliesService familiesService;
    private final IFamilyMembersService familyMembersService;
    private final IFamilyRelationsService familyRelationsService;
    private final KinshipEngine kinshipEngine;   // 亲属称谓计算引擎


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

        FamilyMember member = activeFamilyMemberByUserId(userId);

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
        FamilyMember member = activeFamilyMemberByUserId(userId);
        return member == null ? null : member.getRole();
    }

    @Override
    public String getGenderByUserId(Long userId) {
        FamilyMember member = activeFamilyMemberByUserId(userId);
        return member == null ? null : member.getGender();
    }

    /**
     * 计算 viewer 相对 target 的亲属称谓（如「爸爸」「表哥」）。
     * 若两人不在同一家庭（无关系路径），返回空的称谓（NONE）。
     */
    @Override
    public RelationDTO getRelation(RelationQueryDTO relationQueryDTO) {

        Long viewerUserId = relationQueryDTO.getViewerUserId();
        Long targetUserId = relationQueryDTO.getTargetUserId();

        FamilyMember viewerMember = activeFamilyMemberByUserId(viewerUserId);
        FamilyMember targetMember = activeFamilyMemberByUserId(targetUserId);

        // 任一方不在家庭、或两人不在同一家庭 → 无称谓
        if (viewerMember == null || targetMember == null
                || !viewerMember.getFamilyId().equals(targetMember.getFamilyId())) {
            RelationResult none = RelationResult.NONE;
            return new RelationDTO(none.relationCode());
        }

        Long familyId = viewerMember.getFamilyId();

        // 取出整张家庭关系图的所有边
        List<FamilyRelation> relations = familyRelationsService.lambdaQuery()
                .eq(FamilyRelation::getFamilyId, familyId)
                .isNull(FamilyRelation::getDeletedAt)
                .list();

        // 取出所有成员，做成 id -> 成员 的字典，供引擎按 id 快速查性别/排行
        Map<Long, FamilyMember> membersById = familyMembersService.lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .isNull(FamilyMember::getDeletedAt)
                .list()
                .stream()
                .collect(Collectors.toMap(FamilyMember::getId, m -> m));

        // 交给引擎在图上找路径并翻译成称谓
        RelationResult result = kinshipEngine.computeRelation(
                relations, membersById, viewerMember.getId(), targetMember.getId(),
                relationQueryDTO.getAcceptLanguage()
        );

        return new RelationDTO(result.relationCode());
    }

    /** 按用户 id 查其「在册」的家庭成员记录（一个用户同一时刻至多属于一个家庭，故用 one()） */
    private FamilyMember activeFamilyMemberByUserId(Long userId) {
        return familyMembersService.lambdaQuery()
                .eq(FamilyMember::getUserId, userId)
                .isNull(FamilyMember::getDeletedAt)
                .one();
    }
}
