package asia.sweethome.user.mapper;

import asia.sweethome.user.entity.po.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 【users 表的数据访问接口（Mapper）】
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper} 即自动获得针对 User 的基础增删改查 SQL，无需手写。
 *
 * @author author
 * @since 2026-06-30
 */
public interface UsersMapper extends BaseMapper<User> {

}
