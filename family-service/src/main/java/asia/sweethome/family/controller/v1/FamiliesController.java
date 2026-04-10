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
import asia.sweethome.family.entity.po.FamilyMember;
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
 * 【家庭控制器】
 * <p>
 * 面向前端 App 的家庭相关接口：凭邀请码预览家庭、看家庭详情、看成员列表（含称谓/在线状态）、
 * 生成邀请码、加入家庭。
 * <p>
 * 家庭成员数据在本服务，但「昵称/头像」在 user-service、「在线状态」在 chat-service，
 * 所以本控制器会用 Dubbo 分别去这两个服务取数据再拼装——这是微服务里典型的「数据聚合」。
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
    private UserApi userApi;   // 远程取用户昵称/头像

    @DubboReference
    private ChatApi chatApi;   // 远程取在线状态

    /**
     * 凭邀请码「预览」家庭（还没真正加入）。前端加入前先看看这是哪个家、都有谁，
     * 以便选择「和谁建立什么关系」。
     */
    @GetMapping("/lookup")
    public Result<FamilyLookupVO> lookupByInviteCode(
            @RequestParam("inviteCode") String inviteCode
    ) {
        Family family = familiesService.lookupByInviteCode(inviteCode);

        // 拿到成员后，批量去 user-service 查这些成员的昵称/头像，做成 userId -> 用户 的字典。
        // 「批量查 + 字典」是为了避免在循环里逐个远程调用（N+1 问题）。
        List<FamilyMember> members = familiesService.listActiveMembers(family.getId());
        Map<Long, UserDTO> usersById = userApi.findUsersByIds(
                members.stream().map(FamilyMember::getUserId).toList()
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

    /**
     * 家庭详情（名称、人数、创建时间）。
     * {@code @PathVariable} 把 URL 里的 {familyId} 取出来作为参数。
     * 访问前先校验「当前登录者是这个家庭的成员」，不是成员直接拒绝。
     */
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

    /**
     * 家庭成员列表。这是本控制器最"重"的接口，一次性聚合了四类信息：
     * 成员基本信息（本服务）、昵称头像（user-service）、在线状态（chat-service）、
     * 以及「我」对每位成员的亲属称谓（本地 KinshipEngine 计算）。
     */
    @GetMapping("/{familyId}/members")
    public Result<List<FamilyMemberVO>> getFamilyMembers(
            @PathVariable("familyId") Long familyId,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage
    ) {
        // viewerMember 就是「我」，后面要以我的视角计算对每个人的称谓
        Long viewerUserId = UserContext.getUserId();
        FamilyMember viewerMember = requireActiveMember(familyId, viewerUserId);

        List<FamilyMember> members = familiesService.listActiveMembers(familyId);

        Map<Long, UserDTO> usersById = userApi.findUsersByIds(
                members.stream().map(FamilyMember::getUserId).toList()
        ).stream().collect(Collectors.toMap(UserDTO::getId, u -> u));

        List<FamilyRelation> relations = familyRelationsService.lambdaQuery()
                .eq(FamilyRelation::getFamilyId, familyId)
                .isNull(FamilyRelation::getDeletedAt)
                .list();

        Map<Long, FamilyMember> membersById = members.stream()
                .collect(Collectors.toMap(FamilyMember::getId, m -> m));

        // 查在线状态属于"锦上添花"，即便 chat-service 挂了也不该让整个成员列表打不开。
        // 所以用 try-catch 兜底：失败就当作大家都离线，保证主流程可用（优雅降级）。
        List<Long> resolvedOnlineUserIds;
        try {
            resolvedOnlineUserIds = chatApi.filterOnlineUserIds(
                    members.stream().map(FamilyMember::getUserId).toList()
            );
        } catch (Exception e) {
            log.warn("查询在线状态失败，本次成员列表将全部返回离线", e);
            resolvedOnlineUserIds = List.of();
        }
        // lambda 里要用的外部变量必须是 final（或事实 final），故另赋一个不可变引用
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

            return vo;
        }).toList();

        return Result.success(result);
    }

    /** 生成（或复用）本家庭的邀请码，仅管理员可操作（权限校验在 Service 里做） */
    @PostMapping("/{familyId}/invite")
    public Result<InviteCodeVO> generateInviteCode(
            @PathVariable("familyId") Long familyId
    ) {
        Family family = familiesService.generateInviteCode(familyId, UserContext.getUserId());
        return Result.success(new InviteCodeVO(family.getInviteCode(), family.getInviteExpiresAt()));
    }

    /** 凭邀请码正式加入家庭。加入成功后复用 getFamilyDetail 返回新家庭详情 */
    @PostMapping("/join")
    public Result<FamilyDetailVO> joinFamilyByInviteCode(
            @RequestBody JoinFamilyByInviteCodeDTO joinFamilyByInviteCodeDTO
    ) {

        String inviteCode = joinFamilyByInviteCodeDTO.getInviteCode();
        String gender = joinFamilyByInviteCodeDTO.getGender();
        Long relationToMemberId = joinFamilyByInviteCodeDTO.getRelationToMemberId();
        String relationType = joinFamilyByInviteCodeDTO.getRelationType();

        // invite code 验证
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

    /**
     * 权限小工具：确认 userId 确实是 familyId 的在册成员，是则返回其成员记录，
     * 不是则抛 NOT_FAMILY_MEMBER。避免非成员偷看别人家的信息。
     */
    private FamilyMember requireActiveMember(Long familyId, Long userId) {
        FamilyMember member = familyMembersService.lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, userId)
                .isNull(FamilyMember::getDeletedAt)
                .one();
        if (member == null) {
            throw new BusinessException(ErrorCode.NOT_FAMILY_MEMBER);
        }
        return member;
    }

}
