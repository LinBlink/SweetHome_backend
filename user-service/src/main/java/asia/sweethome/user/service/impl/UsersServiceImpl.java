package asia.sweethome.user.service.impl;

import asia.sweethome.user.entity.po.User;
import asia.sweethome.user.mapper.UsersMapper;
import asia.sweethome.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
