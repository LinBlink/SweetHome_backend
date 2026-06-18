package asia.sweethome.moment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Param;

import asia.sweethome.moment.entity.po.MomentLiker;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-15
 */
public interface MomentLikerMapper extends BaseMapper<MomentLiker> {

    void likeMoment(
            @Param("momentId") Long momentId,
            @Param("likeUserId") Long likeUserId
    );

    Integer getMomentLikeCount(
            @Param("momentId") Long momentId
    );

}
