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
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author author
 * @since 2026-06-30
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, User> implements IUsersService {

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

    @Override
    public User updateProfile(Long userId, String name, String avatarUrl) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (StrUtil.isNotBlank(name)) {
            user.setName(name);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
        return user;
    }

}
