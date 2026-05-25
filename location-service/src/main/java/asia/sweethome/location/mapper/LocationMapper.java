package asia.sweethome.location.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import asia.sweethome.location.entity.po.Location;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-13
 */
public interface LocationMapper extends BaseMapper<Location> {

    List<Location> selectLastestByFamilyId(
            @Param("familyId") Long familyId
    );

}
