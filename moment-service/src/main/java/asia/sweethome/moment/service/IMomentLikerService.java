package asia.sweethome.moment.service;

import com.baomidou.mybatisplus.extension.service.IService;

import asia.sweethome.moment.entity.po.MomentLiker;
import asia.sweethome.moment.entity.vo.MomentLikeDetailsVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-15
 */
public interface IMomentLikerService extends IService<MomentLiker> {

    void likeMoment(Long momentId, Long userId);

    void unlikeMoment(Long momentId, Long userId);

    Integer getMomentLikeCount(Long momentId);

    MomentLikeDetailsVO getMomentLikeDetails(Long momentId);
}
