package asia.sweethome.user.service.dubbo;

import asia.sweethome.api.FamilyApi;
import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.*;
import asia.sweethome.api.entity.vo.UserInfoVO;
import asia.sweethome.common.constants.RoleConstants;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.user.entity.po.User;
import asia.sweethome.user.service.IUsersService;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import cn.hutool.core.bean.BeanUtil;

/**
 * 【UserApi 的 Dubbo 实现】
 * <p>
 * 这是 user-service 对外提供的远程服务（{@code @DubboService} 会把它注册到 Nacos，供其它服务调用）。
 * 它既操作本服务的用户表，又会反过来通过 Dubbo（{@code @DubboReference familyApi}）调用 family-service
 * 完成建家庭/入家庭，是典型的「服务间协作」样板。
 *
 * @author: LOCRIAN_V
 * @date: 6/30/2026 3:59 PM
 */
@DubboService
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final IUsersService usersService;   // 本服务的用户业务

    @DubboReference
    private FamilyApi familyApi;                 // 远程的家庭服务

    /**
     * 按手机号查「用户 + 家庭」信息，供 auth-service 登录时比对密码用。
     * 返回的 UserDTO 含 passwordHash，仅在服务间流转、由 auth-service 内部比对，不会透传给前端。
     */
    @Override
    public UserDTO findUserAndFamilyByPhone(String phone) {

        // 查用户，查不到 findUserByPhone 内部会抛 USER_NOT_FOUND
        User user = usersService.findUserByPhone(phone);

        // 查该用户的家庭与角色（注册流程保证每个用户都有家庭，故此处不会返回 null）
        FamilyDTO family = familyApi.getFamilyByUserId(user.getId());
        String role = familyApi.getFamilyRoleByUserId( user.getId() );

        // 手动逐字段装配成对外的 UserDTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setPhone(user.getPhone());
        userDTO.setName(user.getName());
        userDTO.setAvatarUrl(user.getAvatarUrl());
        userDTO.setPasswordHash(user.getPasswordHash());
        userDTO.setRole( role );
        userDTO.setFamilyId( family.getId() );
        userDTO.setFamilyName( family.getName() );

        return userDTO;

    }

    /**
     * 创建用户，并根据入参「新建家庭」或「加入家庭」，返回带家庭信息的用户 VO。
     * <p>
     * 关于「为什么不加 @Transactional」这一重要设计取舍：<br>
     * 本方法末尾会发起跨服务 Dubbo 调用（family-service 建家庭时，families.created_by 外键要引用
     * 这里刚建的 user id）。如果把用户写入包进一个本地数据库事务，那么在事务提交「之前」发起远程调用时，
     * 对方所在的数据库连接是看不到这条尚未提交的用户记录的，外键约束会立即失败。<br>
     * 所以这里让用户写入依赖 MyBatis-Spring 默认的「单条语句自动提交」，确保发起远程调用前用户已真正落库；
     * 代价是：若下游建家庭失败，这条用户记录不会自动回滚（本项目未引入分布式事务，属已知取舍）。
     */
    @Override
    public UserInfoVO createUser(UserRegisterDTO userRegisterDTO) {

        User newUser = new User();

        // 1. 先查手机号是否已被注册（count 统计条数）
        Long userCount = usersService.lambdaQuery()
                .eq(User::getPhone, userRegisterDTO.getPhone())
                .count();

        if (userCount > 0) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        // 2. 装配并保存用户。注意：密码在 auth-service 已加密，这里存的就是密文
        newUser.setPhone(userRegisterDTO.getPhone());
        newUser.setPasswordHash(userRegisterDTO.getPassword());
        newUser.setName(userRegisterDTO.getName());
        LocalDateTime now = LocalDateTime.now();
        newUser.setCreatedAt(now);
        newUser.setUpdatedAt(now);

        boolean saveSuccess = usersService.save(newUser);

        if (!saveSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // save 之后，自增主键会被 MyBatis-Plus 回填到 newUser.id
        Long newUserId = newUser.getId();

        // 3. 先把用户本身的信息填进返回 VO
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(newUserId);
        userInfoVO.setName(newUser.getName());
        userInfoVO.setPhone(newUser.getPhone());

        // 4. 分支一：带了邀请码 → 加入已有家庭
        String inviteCode = userRegisterDTO.getInviteCode();

        if (StrUtil.isNotBlank(inviteCode)) {

            FamilyJoinInfoDTO familyJoinInfoDTO = new FamilyJoinInfoDTO();
            familyJoinInfoDTO.setUserId(newUserId);
            familyJoinInfoDTO.setGender(userRegisterDTO.getGender());
            familyJoinInfoDTO.setInviteCode(inviteCode);
            familyJoinInfoDTO.setRelationToMemberId(
                    userRegisterDTO.getRelationToMemberId()
            );
            familyJoinInfoDTO.setRelationType(userRegisterDTO.getRelationType());

            // 远程调用 family-service 加入家庭
            FamilyDTO familyDTO = familyApi.joinFamily(
                    familyJoinInfoDTO
            );

            userInfoVO.setFamilyId(familyDTO.getId());
            userInfoVO.setFamilyName(familyDTO.getName());
            // 加入别人家庭的，角色必然是普通成员
            userInfoVO.setRole(RoleConstants.FAMILY_MEMBER);

            return userInfoVO;

        }

        // 5. 分支二：没带邀请码 → 用家庭名新建家庭（家庭名不能为空）
        if (StrUtil.isBlank(userRegisterDTO.getFamilyName())) {
            throw new BusinessException(ErrorCode.FAMILY_NAME_EMPTY);
        }

        FamilyCreateInfoDTO familyCreateInfoDTO = new FamilyCreateInfoDTO();
        familyCreateInfoDTO.setUserId(newUserId);
        familyCreateInfoDTO.setFamilyName(userRegisterDTO.getFamilyName());
        familyCreateInfoDTO.setGender(userRegisterDTO.getGender());

        // 远程调用 family-service 创建家庭
        FamilyDTO familyDTO = familyApi.createFamily(
                familyCreateInfoDTO
        );

        userInfoVO.setFamilyId(familyDTO.getId());
        userInfoVO.setFamilyName(familyDTO.getName());
        // 自己建家庭的，角色必然是管理员
        userInfoVO.setRole(RoleConstants.FAMILY_ADMIN);

        return userInfoVO;
    }

    /** 按 id 查单个用户，转成 UserDTO。BeanUtil.copyProperties 会把同名字段自动拷贝过去 */
    @Override
    public UserDTO findUserById(Long userId) {
        User user = usersService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return BeanUtil.copyProperties(user, UserDTO.class);
    }

    /** 按一批 id 批量查用户；空列表直接返回空，避免无意义的数据库查询 */
    @Override
    public List<UserDTO> findUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        // listByIds 一次性查回所有匹配用户，再逐个转成 DTO（stream 流式处理）
        return usersService.listByIds(userIds).stream()
                .map(u -> BeanUtil.copyProperties(u, UserDTO.class))
                .toList();
    }

}
