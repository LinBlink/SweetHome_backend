package asia.sweethome.auth.mapper;

import asia.sweethome.auth.entity.po.RefreshToken;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 【refresh_tokens 表的数据访问接口（Mapper）】
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，就自动拥有了针对 RefreshToken 的
 * insert/selectById/updateById/delete 等基础 SQL——一行代码不用写。
 * 需要复杂查询时才在这里补自定义方法（配合 XML 或注解）。
 */
public interface RefreshTokensMapper extends BaseMapper<RefreshToken> {
}
