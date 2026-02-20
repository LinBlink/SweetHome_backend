package asia.sweethome.family.mapper;

import asia.sweethome.family.entity.po.Family;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 【families 表 Mapper】继承 BaseMapper 即自动获得基础增删改查 SQL。
 * 复杂 SQL 可写在同名的 resources/mapper/FamiliesMapper.xml 中。
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
public interface FamiliesMapper extends BaseMapper<Family> {

}
