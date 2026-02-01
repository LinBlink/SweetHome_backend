package asia.sweethome.family.service.impl;

import asia.sweethome.api.ChatApi;
import asia.sweethome.common.constants.RelationTypeConstants;
import asia.sweethome.common.constants.RoleConstants;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.family.entity.po.Family;
import asia.sweethome.family.entity.po.FamilyMemeber;
import asia.sweethome.family.entity.po.FamilyRelation;
import asia.sweethome.family.mapper.FamiliesMapper;
import asia.sweethome.family.service.IFamiliesService;
import asia.sweethome.family.service.IFamilyMembersService;
import asia.sweethome.family.service.IFamilyRelationsService;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FamiliesServiceImpl extends ServiceImpl<FamiliesMapper, Family> implements IFamiliesService {

    private static final String INVITE_CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 8;
    private static final long INVITE_CODE_TTL_HOURS = 48;

    @Autowired
    private IFamilyMembersService familyMembersService;

    @Autowired
    private IFamilyRelationsService familyRelationsService;

    @DubboReference
    private ChatApi chatApi;

    @Override
    /**
     * 加入一个家庭需要经过的步骤
     *      邀请码有效性检查
     *      退出旧家庭（级联）
     *      新增 FamilyMemeber
     *      根据锚点完善 FamilyRelation
     *      加入新家庭群聊
     *      返回家庭 id
     *
     * 注意：不加 @Transactional —— 本方法末尾及 leaveOldFamily 内部都会跨服务调用 chat-service
     * （建群聊/加入群聊/退出群聊），chat-service 对应的外键（conversations.family_id 等）要求
     * family-service 这边的写入已经真正落库。若整体包一层本地事务，跨服务调用发起时这边的数据
     * 在其他连接看来仍是未提交状态，外键约束会失败。这里依赖各次 save/update 的单语句自动提交，
     * 代价是中途失败不会整体回滚（无分布式事务，属已知取舍，与 UserApiImpl.createUser 一致）。
     */
    public Long joinFamily(
            Long userId,
            String inviteCode,
            String gender,
            Long relationToMemberId,
            String relationType
    ) {

        // 邀请码有效性检查
        inviteCode = inviteCode.trim().toUpperCase();

        // 查找到邀请码对应的家庭
        Family family = lambdaQuery().eq(Family::getInviteCode, inviteCode).one();
        if (family == null) {
            throw new BusinessException(ErrorCode.INVITE_CODE_INVALID);
        }

        LocalDateTime now = LocalDateTime.now();
        if (family.getInviteExpiresAt() == null || family.getInviteExpiresAt().isBefore(now)) {
            throw new BusinessException(ErrorCode.INVITE_CODE_INVALID);
        }

        // 得到邀请码家庭的ID
        Long familyId = family.getId();

        // relationToMemberId 必须属于同一个家庭，且未被软删除
        //      即，该锚点成员存在，需要凭借该锚点成员完善族谱
        FamilyMemeber anchor = familyMembersService.lambdaQuery()
                .eq(FamilyMemeber::getId, relationToMemberId)
                .eq(FamilyMemeber::getFamilyId, familyId)
                .isNull(FamilyMemeber::getDeletedAt)
                .one();
        if (anchor == null) {
            throw new BusinessException(ErrorCode.INVALID_RELATION_ANCHOR);
        }

        // 0. 同一时刻只能属于一个家庭：先级联退出旧家庭（若有）
        FamilyMemeber existingActiveMembership = familyMembersService.lambdaQuery()
                .eq(FamilyMemeber::getUserId, userId)
                .isNull(FamilyMemeber::getDeletedAt)
                .one();
        if (existingActiveMembership != null) {
            leaveOldFamily(existingActiveMembership);
        }

        // 1. 新成员加入 family_members
        FamilyMemeber newMember = new FamilyMemeber();
        newMember.setFamilyId(familyId);
        newMember.setUserId(userId);
        newMember.setRole(RoleConstants.FAMILY_MEMBER);
        newMember.setGender(gender);
        newMember.setJoinedAt(now);
        boolean memberSaved = familyMembersService.save(newMember);
        if (!memberSaved) {
            throw new BusinessException(ErrorCode.FAMILY_SAVE_FAILURE);
        }

        // 新成员的 memeberId
        Long newMemberId = newMember.getId();

        // 2. 按 relationType 写 family_relations
        switch (relationType) {
            case RelationTypeConstants.CHILD_OF -> {
                addParentOf(familyId, anchor.getId(), newMemberId);
                // 若锚点已有配偶，同时给配偶也补一条直接血亲边，
                // 否则配偶视角算出来的关系会退化成"配偶的儿子"而不是"儿子"
                Long anchorSpouseId = findSpouseId(anchor.getId());
                if (anchorSpouseId != null) {
                    addParentOf(familyId, anchorSpouseId, newMemberId);
                }
            }
            case RelationTypeConstants.PARENT_OF -> addParentOf(familyId, newMemberId, anchor.getId());
            case RelationTypeConstants.SPOUSE_OF -> {
                if (findSpouseId(anchor.getId()) != null) {
                    throw new BusinessException(ErrorCode.SPOUSE_ALREADY_EXISTS);
                }
                addSpouseOf(familyId, newMemberId, anchor.getId());
            }
            case RelationTypeConstants.SIBLING_OF -> {
                List<Long> anchorParentIds = familyRelationsService.lambdaQuery()
                        .eq(FamilyRelation::getRelationType, RelationTypeConstants.PARENT_OF)
                        .eq(FamilyRelation::getObjectMemberId, anchor.getId())
                        .isNull(FamilyRelation::getDeletedAt)
                        .list()
                        .stream()
                        .map(FamilyRelation::getSubjectMemberId)
                        .toList();
                if (anchorParentIds.isEmpty()) {
                    throw new BusinessException(ErrorCode.NO_KNOWN_PARENT);
                }
                anchorParentIds.forEach(parentId -> addParentOf(familyId, parentId, newMemberId));
            }
            default -> throw new BusinessException(ErrorCode.INVALID_RELATION_TYPE);
        }

        // 3. 加入新家庭群聊
        chatApi.addMemberToGroupConversation(familyId, userId);

        return familyId;
    }

    @Override
    public Long createFamily(
            Long userId,
            String gender,
            String familyName
    ) {
        Family family = new Family();
        LocalDateTime now = LocalDateTime.now();
        family.setName(familyName);
        family.setCreatedBy(userId);
        family.setCreatedAt(now);
        family.setUpdatedAt(now);

        // 保存家庭
        boolean familySaved = save(family);
        if (!familySaved) {
            throw new BusinessException(ErrorCode.FAMILY_SAVE_FAILURE);
        }

        // 保存家庭成员
        FamilyMemeber familyMemeber = new FamilyMemeber();
        familyMemeber.setFamilyId(family.getId());
        familyMemeber.setUserId(userId);
        familyMemeber.setRole(RoleConstants.FAMILY_ADMIN);
        familyMemeber.setGender(gender);
        familyMemeber.setJoinedAt(now);

        familyMembersService.save(familyMemeber);

        // 自动创建家庭群聊
        chatApi.createGroupConversation(family.getId(), familyName + "群聊", List.of(userId));

        return family.getId();
    }

    @Override
    @Transactional
    public Family generateInviteCode(Long familyId, Long requesterUserId) {

        FamilyMemeber requester = familyMembersService.lambdaQuery()
                .eq(FamilyMemeber::getFamilyId, familyId)
                .eq(FamilyMemeber::getUserId, requesterUserId)
                .isNull(FamilyMemeber::getDeletedAt)
                .one();
        if (requester == null) {
            throw new BusinessException(ErrorCode.NOT_FAMILY_MEMBER);
        }
        if (!RoleConstants.FAMILY_ADMIN.equals(requester.getRole())) {
            throw new BusinessException(ErrorCode.NOT_FAMILY_ADMIN);
        }

        Family family = getById(familyId);
        if (family == null) {
            throw new BusinessException(ErrorCode.NO_SUCH_FAMILY);
        }

        LocalDateTime now = LocalDateTime.now();

        // 已存在未过期的邀请码时直接返回现有邀请码
        if (StrUtil.isNotBlank(family.getInviteCode())
                && family.getInviteExpiresAt() != null
                && family.getInviteExpiresAt().isAfter(now)) {
            return family;
        }

        family.setInviteCode(RandomUtil.randomString(INVITE_CODE_ALPHABET, INVITE_CODE_LENGTH));
        family.setInviteExpiresAt(now.plusHours(INVITE_CODE_TTL_HOURS));
        family.setUpdatedAt(now);
        updateById(family);

        return family;
    }

    @Override
    public Family lookupByInviteCode(String inviteCode) {
        String code = inviteCode == null ? null : inviteCode.trim().toUpperCase();
        if (StrUtil.isBlank(code)) {
            throw new BusinessException(ErrorCode.FAMILY_INVITE_CODE_EMPTY);
        }

        Family family = lambdaQuery().eq(Family::getInviteCode, code).one();
        LocalDateTime now = LocalDateTime.now();
        if (family == null || family.getInviteExpiresAt() == null || family.getInviteExpiresAt().isBefore(now)) {
            throw new BusinessException(ErrorCode.INVITE_CODE_INVALID);
        }
        return family;
    }

    @Override
    public List<FamilyMemeber> listActiveMembers(Long familyId) {
        return familyMembersService.lambdaQuery()
                .eq(FamilyMemeber::getFamilyId, familyId)
                .isNull(FamilyMemeber::getDeletedAt)
                .list();
    }

    /**
     * 退出旧家庭级联：软删除成员记录 + 关系边 + 退出旧群聊 + 必要时转正新管理员/软删除空家庭。
     * <p>
     * 注意：deletedAt 被 common-mybatis.yaml 配置为全局逻辑删除字段（logic-delete-field），
     * MyBatis-Plus 对逻辑删除字段的 updateById 会静默忽略手动赋的值（框架认为这个字段只应该
     * 由 remove()/removeById() 触发的逻辑删除 SQL 来改写），直接 entity.setDeletedAt(now)
     * 后再 updateById 不会真正落库。必须用 removeById/removeByIds 才能让 deleted_at 生效。
     */
    private void leaveOldFamily(FamilyMemeber oldMember) {

        Long oldFamilyId = oldMember.getFamilyId();

        // 1. 软删除该成员记录
        familyMembersService.removeById(oldMember.getId());

        // 2. 软删除其在旧家庭关系图中的全部边
        List<FamilyRelation> edges = familyRelationsService.lambdaQuery()
                .eq(FamilyRelation::getFamilyId, oldFamilyId)
                .and(w -> w.eq(FamilyRelation::getSubjectMemberId, oldMember.getId())
                        .or()
                        .eq(FamilyRelation::getObjectMemberId, oldMember.getId()))
                .isNull(FamilyRelation::getDeletedAt)
                .list();
        if (!edges.isEmpty()) {
            familyRelationsService.removeByIds(edges.stream().map(FamilyRelation::getId).toList());
        }

        // 3. 退出旧家庭群聊（不影响与旧家庭成员的私聊）
        chatApi.removeMemberFromGroupConversation(oldFamilyId, oldMember.getUserId());

        // 4. 若退出者是管理员，需要转正下一位或软删除空家庭
        if (RoleConstants.FAMILY_ADMIN.equals(oldMember.getRole())) {
            List<FamilyMemeber> remaining = familyMembersService.lambdaQuery()
                    .eq(FamilyMemeber::getFamilyId, oldFamilyId)
                    .isNull(FamilyMemeber::getDeletedAt)
                    .orderByAsc(FamilyMemeber::getJoinedAt)
                    .list();

            if (remaining.isEmpty()) {
                removeById(oldFamilyId);
            } else {
                FamilyMemeber successor = remaining.get(0);
                successor.setRole(RoleConstants.FAMILY_ADMIN);
                familyMembersService.updateById(successor);
            }
        }
    }

    // 找到配偶的 memeberId
    private Long findSpouseId(Long memberId) {

        FamilyRelation rel = familyRelationsService.lambdaQuery()
                // 先查所有的 SPOUSE_OF 关系
                .eq(FamilyRelation::getRelationType, RelationTypeConstants.SPOUSE_OF)
                .and(
                        // AND 下面的语句
                        w ->
                                // 配偶的双向性
                                w.eq(FamilyRelation::getSubjectMemberId, memberId)
                                        .or()
                                        .eq(FamilyRelation::getObjectMemberId, memberId)
                )
                .isNull(FamilyRelation::getDeletedAt)
                .one();
        if (rel == null) return null;
        return rel.getSubjectMemberId().equals(memberId) ? rel.getObjectMemberId() : rel.getSubjectMemberId();
    }

    private void addParentOf(Long familyId, Long parentMemberId, Long childMemberId) {
        FamilyRelation rel = new FamilyRelation();
        rel.setFamilyId(familyId);
        rel.setSubjectMemberId(parentMemberId);
        rel.setRelationType(RelationTypeConstants.PARENT_OF);
        rel.setObjectMemberId(childMemberId);
        rel.setCreatedAt(LocalDateTime.now());
        familyRelationsService.save(rel);
    }

    private void addSpouseOf(Long familyId, Long memberIdA, Long memberIdB) {
        // 规范化，保证 SPOUSE_OF 无向边不会存两次
        long subject = Math.min(memberIdA, memberIdB);
        long object = Math.max(memberIdA, memberIdB);
        FamilyRelation rel = new FamilyRelation();
        rel.setFamilyId(familyId);
        rel.setSubjectMemberId(subject);
        rel.setRelationType(RelationTypeConstants.SPOUSE_OF);
        rel.setObjectMemberId(object);
        rel.setCreatedAt(LocalDateTime.now());
        familyRelationsService.save(rel);
    }
}
