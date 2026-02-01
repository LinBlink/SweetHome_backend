package asia.sweethome.family.controller.v1;


import asia.sweethome.api.ChatApi;
import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.family.entity.dto.JoinFamilyByInviteCodeDTO;
import asia.sweethome.family.entity.po.Family;
import asia.sweethome.family.entity.po.FamilyMemeber;
import asia.sweethome.family.entity.po.FamilyRelation;
import asia.sweethome.family.entity.vo.FamilyDetailVO;
import asia.sweethome.family.entity.vo.FamilyLookupMemberVO;
import asia.sweethome.family.entity.vo.FamilyLookupVO;
import asia.sweethome.family.entity.vo.FamilyMemberVO;
import asia.sweethome.family.entity.vo.InviteCodeVO;
import asia.sweethome.family.kinship.KinshipEngine;
import asia.sweethome.family.kinship.RelationResult;
import asia.sweethome.family.service.IFamiliesService;
import asia.sweethome.family.service.IFamilyMembersService;
import asia.sweethome.family.service.IFamilyRelationsService;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 家庭表 前端控制器
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
@RestController
@RequestMapping("/v1/families")
@Slf4j
@RequiredArgsConstructor
public class FamiliesController {

    private final IFamiliesService familiesService;
    private final IFamilyMembersService familyMembersService;
    private final IFamilyRelationsService familyRelationsService;
    private final KinshipEngine kinshipEngine;

    @DubboReference
    private UserApi userApi;

    @DubboReference
    private ChatApi chatApi;

    @GetMapping("/lookup")
    public Result<FamilyLookupVO> lookupByInviteCode(
            @RequestParam("inviteCode") String inviteCode
    ) {
        Family family = familiesService.lookupByInviteCode(inviteCode);

        List<FamilyMemeber> members = familiesService.listActiveMembers(family.getId());
        Map<Long, UserDTO> usersById = userApi.findUsersByIds(
                members.stream().map(FamilyMemeber::getUserId).toList()
        ).stream().collect(Collectors.toMap(UserDTO::getId, u -> u));

        FamilyLookupVO vo = new FamilyLookupVO();
        vo.setFamilyId(family.getId());
        vo.setFamilyName(family.getName());
        vo.setMembers(members.stream().map(m -> {
            FamilyLookupMemberVO memberVO = new FamilyLookupMemberVO();
            memberVO.setMemberId(m.getId());
            memberVO.setGender(m.getGender());
            UserDTO user = usersById.get(m.getUserId());
            if (user != null) {
                memberVO.setName(user.getName());
                memberVO.setAvatarUrl(user.getAvatarUrl());
            }
            return memberVO;
        }).toList());

        return Result.success(vo);
    }

    @GetMapping("/{familyId}")
    public Result<FamilyDetailVO> getFamilyDetail(
            @PathVariable("familyId") Long familyId
    ) {
        requireActiveMember(familyId, UserContext.getUserId());

        Family family = familiesService.getById(familyId);

        if (family == null) {
            throw new BusinessException(
                    ErrorCode.NO_SUCH_FAMILY
            );
        }

        FamilyDetailVO familyDetailVO = new FamilyDetailVO();

        Integer familyMemberCount = familyMembersService.getFamilyMemberCount(familyId);

        familyDetailVO.setFamilyId(family.getId());
        familyDetailVO.setName(family.getName());
        familyDetailVO.setMemberCount(familyMemberCount);
        familyDetailVO.setCreatedAt(family.getCreatedAt());

        return Result.success(familyDetailVO);

    }

    @GetMapping("/{familyId}/members")
    public Result<List<FamilyMemberVO>> getFamilyMembers(
            @PathVariable("familyId") Long familyId,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        Long viewerUserId = UserContext.getUserId();
        FamilyMemeber viewerMember = requireActiveMember(familyId, viewerUserId);

        List<FamilyMemeber> members = familiesService.listActiveMembers(familyId);

        Map<Long, UserDTO> usersById = userApi.findUsersByIds(
                members.stream().map(FamilyMemeber::getUserId).toList()
        ).stream().collect(Collectors.toMap(UserDTO::getId, u -> u));

        List<FamilyRelation> relations = familyRelationsService.lambdaQuery()
                .eq(FamilyRelation::getFamilyId, familyId)
                .isNull(FamilyRelation::getDeletedAt)
                .list();

        Map<Long, FamilyMemeber> membersById = members.stream()
                .collect(Collectors.toMap(FamilyMemeber::getId, m -> m));

        List<Long> resolvedOnlineUserIds;
        try {
            resolvedOnlineUserIds = chatApi.filterOnlineUserIds(
                    members.stream().map(FamilyMemeber::getUserId).toList()
            );
        } catch (Exception e) {
            log.warn("查询在线状态失败，本次成员列表将全部返回离线", e);
            resolvedOnlineUserIds = List.of();
        }
        final List<Long> onlineUserIds = resolvedOnlineUserIds;

        List<FamilyMemberVO> result = members.stream().map(m -> {
            FamilyMemberVO vo = new FamilyMemberVO();
            vo.setUserId(m.getUserId());
            vo.setGender(m.getGender());
            vo.setRole(m.getRole());
            vo.setIsOnline(onlineUserIds.contains(m.getUserId()));

            UserDTO user = usersById.get(m.getUserId());
            if (user != null) {
                vo.setName(user.getName());
                vo.setAvatarUrl(user.getAvatarUrl());
            }

            RelationResult relation = kinshipEngine.computeRelation(
                    relations, membersById, viewerMember.getId(), m.getId(), acceptLanguage
            );
            vo.setRelationCode(relation.relationCode());
            vo.setRelationLabel(relation.relationLabel());

            return vo;
        }).toList();

        return Result.success(result);
    }

    @PostMapping("/{familyId}/invite")
    public Result<InviteCodeVO> generateInviteCode(
            @PathVariable("familyId") Long familyId
    ) {
        Family family = familiesService.generateInviteCode(familyId, UserContext.getUserId());
        return Result.success(new InviteCodeVO(family.getInviteCode(), family.getInviteExpiresAt()));
    }

    @PostMapping("/join")
    public Result<FamilyDetailVO> joinFamilyByInviteCode(
            @RequestBody JoinFamilyByInviteCodeDTO joinFamilyByInviteCodeDTO
    ) {

        String inviteCode = joinFamilyByInviteCodeDTO.getInviteCode();
        String gender = joinFamilyByInviteCodeDTO.getGender();
        Long relationToMemberId = joinFamilyByInviteCodeDTO.getRelationToMemberId();
        String relationType = joinFamilyByInviteCodeDTO.getRelationType();

        if (StrUtil.isBlank(inviteCode)) {
            throw new BusinessException(ErrorCode.FAMILY_INVITE_CODE_EMPTY);
        }
        if (!"male".equals(gender) && !"female".equals(gender)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        if (relationToMemberId == null || StrUtil.isBlank(relationType)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();

        // 完成加入家庭逻辑（含退出旧家庭级联）
        Long familyId = familiesService.joinFamily(userId, inviteCode.trim().toUpperCase(), gender, relationToMemberId, relationType);

        return getFamilyDetail(familyId);

    }

    private FamilyMemeber requireActiveMember(Long familyId, Long userId) {
        FamilyMemeber member = familyMembersService.lambdaQuery()
                .eq(FamilyMemeber::getFamilyId, familyId)
                .eq(FamilyMemeber::getUserId, userId)
                .isNull(FamilyMemeber::getDeletedAt)
                .one();
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_FAMILY_MEMBER);
        }
        return member;
    }

}
