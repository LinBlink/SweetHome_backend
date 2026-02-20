package asia.sweethome.user.service.impl;

import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.user.entity.po.User;
import asia.sweethome.user.mapper.UsersMapper;
import asia.sweethome.user.service.IUsersService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 【用户表 服务实现类】
 * <p>
 * 继承 MyBatis-Plus 的 {@link ServiceImpl}，自带通用 CRUD；这里补两个业务方法。
 *
 * @author author
 * @since 2026-06-30
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, User> implements IUsersService {

    /** 按手机号查用户，查不到直接抛「用户不存在」，让调用方不必重复判空 */
    @Override
    public User findUserByPhone(String phone) {
        User one = lambdaQuery().eq(
                User::getPhone, phone
        ).one();

        if (one == null) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND
            );
        }

        return one;
    }

    /**
     * 更新个人资料（昵称、头像）。采用「部分更新」策略：
     * 传了才改，没传（null / 空白）就保持原值，避免把用户没打算修改的字段冲成空。
     */
    @Override
    public User updateProfile(Long userId, String name, String avatarUrl) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // StrUtil.isNotBlank：非 null 且非纯空白才更新昵称
        if (StrUtil.isNotBlank(name)) {
            user.setName(name);
        }
        // 头像允许显式清空，所以只判 null（传空字符串视为清空头像）
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);   // 按主键更新这条记录
        return user;
    }

}
