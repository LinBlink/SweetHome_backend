package asia.sweethome.family.service.impl;

import asia.sweethome.api.ChatApi;
import asia.sweethome.common.constants.RelationTypeConstants;
import asia.sweethome.common.constants.RoleConstants;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.family.entity.po.Family;
import asia.sweethome.family.entity.po.FamilyMember;
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

/**
 * 【家庭核心业务实现】
 * <p>
 * 这是全项目最复杂的一块：家庭的创建、加入（含退出旧家庭级联）、邀请码、关系图维护，
 * 都在这里。理解它的关键是把「家庭成员」看作图上的节点、「亲属关系」看作节点之间的边
 * （见 family_relations 表），加入家庭时要正确地往这张图里补边。
 *
 * @author LocrianFifth
 */
@Service
public class FamiliesServiceImpl extends ServiceImpl<FamiliesMapper, Family> implements IFamiliesService {

    // 邀请码使用的字符表（去掉了容易混淆的字符可按需调整）
    private static final String INVITE_CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 8;   // 邀请码长度
    private static final long INVITE_CODE_TTL_HOURS = 48; // 邀请码有效期（小时）

    // 这三者都是同一个 family-service 内的其它 Service / 远程服务
    @Autowired
    private IFamilyMembersService familyMembersService;   // 成员表业务

    @Autowired
    private IFamilyRelationsService familyRelationsService; // 关系图业务

    @DubboReference
    private ChatApi chatApi;   // 远程聊天服务（建群/加群/退群）

    /**
     * 加入一个家庭，依次完成：
     * <ol>
     *   <li>邀请码有效性检查（存在且未过期）；</li>
     *   <li>锚点成员校验（要和家里谁建立关系，那个人必须真实存在于该家庭）；</li>
     *   <li>若用户已在别的家庭，先级联退出旧家庭；</li>
     *   <li>新增 family_members 记录；</li>
     *   <li>按 relationType 往关系图补边（父子/夫妻/兄弟姐妹）；</li>
     *   <li>加入新家庭群聊；返回家庭 id。</li>
     * </ol>
     * <p>
     * 关于「为什么不加 @Transactional」：本方法末尾及 leaveOldFamily 内部都会跨服务调用 chat-service
     * （建群聊/加入群聊/退出群聊），chat-service 对应的外键（conversations.family_id 等）要求
     * family-service 这边的写入已经真正落库。若整体包一层本地事务，跨服务调用发起时这边的数据
     * 在其他连接看来仍是未提交状态，外键约束会失败。这里依赖各次 save/update 的单语句自动提交，
     * 代价是中途失败不会整体回滚（无分布式事务，属已知取舍，与 UserApiImpl.createUser 一致）。
     */
    @Override
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
        FamilyMember anchor = familyMembersService.lambdaQuery()
                .eq(FamilyMember::getId, relationToMemberId)
                .eq(FamilyMember::getFamilyId, familyId)
                .isNull(FamilyMember::getDeletedAt)
                .one();
        if (anchor == null) {
            throw new BusinessException(ErrorCode.INVALID_RELATION_ANCHOR);
        }

        // 0. 同一时刻只能属于一个家庭：先级联退出旧家庭（若有）
        FamilyMember existingActiveMembership = familyMembersService.lambdaQuery()
                .eq(FamilyMember::getUserId, userId)
                .isNull(FamilyMember::getDeletedAt)
                .one();
        if (existingActiveMembership != null) {
            leaveOldFamily(existingActiveMembership);
        }

        // 1. 新成员加入 family_members
        FamilyMember newMember = new FamilyMember();
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

        // 2. 按 relationType 往关系图补边。注意 relationType 描述的是「新成员相对锚点」的关系。
        switch (relationType) {
            // 新成员是锚点的「孩子」：补一条 锚点 --PARENT_OF--> 新成员 的边
            case RelationTypeConstants.CHILD_OF -> {
                addParentOf(familyId, anchor.getId(), newMemberId);
                // 若锚点已有配偶，同时给配偶也补一条直接血亲边，
                // 否则配偶视角算出来的关系会退化成"配偶的儿子"而不是"儿子"
                Long anchorSpouseId = findSpouseId(anchor.getId());
                if (anchorSpouseId != null) {
                    addParentOf(familyId, anchorSpouseId, newMemberId);
                }
            }
            // 新成员是锚点的「父/母」：方向反过来，新成员 --PARENT_OF--> 锚点
            case RelationTypeConstants.PARENT_OF -> addParentOf(familyId, newMemberId, anchor.getId());
            // 新成员是锚点的「配偶」：一夫一妻，锚点已有配偶则报错
            case RelationTypeConstants.SPOUSE_OF -> {
                if (findSpouseId(anchor.getId()) != null) {
                    throw new BusinessException(ErrorCode.SPOUSE_ALREADY_EXISTS);
                }
                addSpouseOf(familyId, newMemberId, anchor.getId());
            }
            // 新成员是锚点的「兄弟姐妹」：本质是「和锚点共享父母」，
            // 所以把锚点的每一位父母，也都设为新成员的父母
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

    /**
     * 创建家庭：建家庭记录 → 把创建者作为管理员加入成员表 → 自动建家庭群聊 → 返回家庭 id。
     * 同样不加 @Transactional，原因见 joinFamily 的说明。
     */
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
        FamilyMember familyMember = new FamilyMember();
        familyMember.setFamilyId(family.getId());
        familyMember.setUserId(userId);
        familyMember.setRole(RoleConstants.FAMILY_ADMIN);
        familyMember.setGender(gender);
        familyMember.setJoinedAt(now);

        familyMembersService.save(familyMember);

        // 自动创建家庭群聊
        chatApi.createGroupConversation(family.getId(), familyName + "群聊", List.of(userId));

        return family.getId();
    }

    /**
     * 生成邀请码（仅管理员可操作）。若当前已有未过期的邀请码则直接复用，避免频繁刷新。
     * 这个方法只改自己一张表、不跨服务，所以可以安全地加 @Transactional 保证原子性。
     */
    @Override
    @Transactional
    public Family generateInviteCode(Long familyId, Long requesterUserId) {

        // 先确认请求者确实是这个家庭的成员，且角色是管理员
        FamilyMember requester = familyMembersService.lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, requesterUserId)
                .isNull(FamilyMember::getDeletedAt)
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

    /**
     * 凭邀请码查家庭（加入前的「预览」用）。邀请码不区分大小写，统一转大写再查；
     * 家庭不存在或邀请码已过期都抛 INVITE_CODE_INVALID。
     */
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

    /** 列出某家庭所有「在册」成员（deletedAt 为全局逻辑删除字段，已退出的会被自动过滤） */
    @Override
    public List<FamilyMember> listActiveMembers(Long familyId) {
        return familyMembersService.lambdaQuery()
                .eq(FamilyMember::getFamilyId, familyId)
                .isNull(FamilyMember::getDeletedAt)
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
    private void leaveOldFamily(FamilyMember oldMember) {

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
            List<FamilyMember> remaining = familyMembersService.lambdaQuery()
                    .eq(FamilyMember::getFamilyId, oldFamilyId)
                    .isNull(FamilyMember::getDeletedAt)
                    .orderByAsc(FamilyMember::getJoinedAt)
                    .list();

            if (remaining.isEmpty()) {
                removeById(oldFamilyId);
            } else {
                FamilyMember successor = remaining.get(0);
                successor.setRole(RoleConstants.FAMILY_ADMIN);
                familyMembersService.updateById(successor);
            }
        }
    }

    /**
     * 找出某成员的配偶 id。配偶边是「无向」的（谁是 subject 谁是 object 不固定），
     * 所以要同时匹配 subject=memberId 或 object=memberId 两种情况，命中后返回「另一头」。
     * 没有配偶时返回 null。
     */
    private Long findSpouseId(Long memberId) {

        FamilyRelation rel = familyRelationsService.lambdaQuery()
                // 先限定关系类型为「配偶」
                .eq(FamilyRelation::getRelationType, RelationTypeConstants.SPOUSE_OF)
                .and(
                        // 再要求 memberId 出现在边的任意一端（配偶边无向）
                        w -> w.eq(FamilyRelation::getSubjectMemberId, memberId)
                                .or()
                                .eq(FamilyRelation::getObjectMemberId, memberId)
                )
                .isNull(FamilyRelation::getDeletedAt)
                .one();
        if (rel == null) return null;
        // 返回边上「不是自己」的那一端，即配偶
        return rel.getSubjectMemberId().equals(memberId) ? rel.getObjectMemberId() : rel.getSubjectMemberId();
    }

    /** 往关系图写一条有向的父子边：parent --PARENT_OF--> child */
    private void addParentOf(Long familyId, Long parentMemberId, Long childMemberId) {
        FamilyRelation rel = new FamilyRelation();
        rel.setFamilyId(familyId);
        rel.setSubjectMemberId(parentMemberId);   // subject = 父/母
        rel.setRelationType(RelationTypeConstants.PARENT_OF);
        rel.setObjectMemberId(childMemberId);     // object = 子/女
        rel.setCreatedAt(LocalDateTime.now());
        familyRelationsService.save(rel);
    }

    /** 往关系图写一条无向的配偶边 */
    private void addSpouseOf(Long familyId, Long memberIdA, Long memberIdB) {
        // 规范化：始终让较小的 id 当 subject、较大的当 object。
        // 这样 (3,5) 和 (5,3) 都会存成 (3,5)，避免同一对配偶被存成两行重复数据。
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
