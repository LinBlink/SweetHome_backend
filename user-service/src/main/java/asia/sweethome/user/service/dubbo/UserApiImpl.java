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
 * @description:
 * @author: LOCRIAN_V
 * @date: 6/30/2026 3:59 PM
 */
@DubboService
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final IUsersService usersService;

    @DubboReference
    private FamilyApi familyApi;

    // 找到用户后不能直接返回前端所需用户数据，需要校验密码
    @Override
    public UserDTO findUserAndFamilyByPhone(String phone) {

        User user = usersService.findUserByPhone(phone);

        FamilyDTO family = familyApi.getFamilyByUserId(user.getId());
        String role = familyApi.getFamilyRoleByUserId( user.getId() );

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

    // 创建用户后直接返回前端所需用户数据
    //
    // 注意：不能给整个方法加 @Transactional —— 方法末尾会发起跨服务 Dubbo 调用（family-service
    // 建家庭时会用 families.created_by 外键引用这里刚建的 user id）。若把用户写入包进本地事务，
    // 在事务提交前发起远程调用，对方连接是看不到这条未提交记录的，外键约束会直接失败。
    // 这里让用户写入依赖 MyBatis-Spring 默认的单条语句自动提交，保证远程调用发起前数据已落库；
    // 代价是如果下游建家庭失败，这条用户记录不会被自动回滚（无分布式事务，属已知取舍）。
    @Override
    public UserInfoVO createUser(UserRegisterDTO userRegisterDTO) {

        User newUser = new User();

        // 查询用户是否已经注册
        Long userCount = usersService.lambdaQuery()
                .eq(User::getPhone, userRegisterDTO.getPhone())
                .count();

        if (userCount > 0) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        newUser.setPhone(userRegisterDTO.getPhone());
        // 此时的密码已经被处理过
        newUser.setPasswordHash(userRegisterDTO.getPassword());
        newUser.setName(userRegisterDTO.getName());
        LocalDateTime now = LocalDateTime.now();
        newUser.setCreatedAt(now);
        newUser.setUpdatedAt(now);

        boolean saveSuccess = usersService.save(newUser);

        // 完成用户创建

        if (!saveSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        Long newUserId = newUser.getId();

        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(newUserId);
        userInfoVO.setName(newUser.getName());
        userInfoVO.setPhone(newUser.getPhone());

        // 如果有 inviteCode ，根据 inviteCode 去加入家庭

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


            FamilyDTO familyDTO = familyApi.joinFamily(
                    familyJoinInfoDTO
            );

            userInfoVO.setFamilyId(familyDTO.getId());
            userInfoVO.setFamilyName(familyDTO.getName());
            // 加入家庭的必然是 MEMBER
            userInfoVO.setRole(RoleConstants.FAMILY_MEMBER);

            return userInfoVO;

        }

        // 如果没有 inviteCode ，根据家庭名创建家庭

        if (StrUtil.isBlank(userRegisterDTO.getFamilyName())) {
            throw new BusinessException(ErrorCode.FAMILY_NAME_EMPTY);
        }

        FamilyCreateInfoDTO familyCreateInfoDTO = new FamilyCreateInfoDTO();

        familyCreateInfoDTO.setUserId(newUserId);
        familyCreateInfoDTO.setFamilyName(userRegisterDTO.getFamilyName());
        familyCreateInfoDTO.setGender(userRegisterDTO.getGender());


        FamilyDTO familyDTO = familyApi.createFamily(
                familyCreateInfoDTO
        );

        userInfoVO.setFamilyId(familyDTO.getId());
        userInfoVO.setFamilyName(familyDTO.getName());
        // 创建家庭的必然是 admin
        userInfoVO.setRole(RoleConstants.FAMILY_ADMIN);

        return userInfoVO;
    }

    @Override
    public UserDTO findUserById(Long userId) {
        User user = usersService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return BeanUtil.copyProperties(user, UserDTO.class);
    }

    @Override
    public List<UserDTO> findUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return usersService.listByIds(userIds).stream()
                .map(u -> BeanUtil.copyProperties(u, UserDTO.class))
                .toList();
    }

}
