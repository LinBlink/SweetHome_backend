package asia.sweethome.user.service;

import asia.sweethome.user.entity.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 【用户表 服务接口】
 * <p>
 * 继承 MyBatis-Plus 的 {@link IService} 白得一堆通用 CRUD 方法；这里再声明两个业务方法。
 * 实现见 {@link asia.sweethome.user.service.impl.UsersServiceImpl}。
 *
 * @author author
 * @since 2026-06-30
 */
public interface IUsersService extends IService<User> {

    // 按手机号查用户（查不到会抛业务异常）
    User findUserByPhone(String phone);

    // 部分更新个人资料（昵称、头像）
    User updateProfile(Long userId, String name, String avatarUrl);
}
