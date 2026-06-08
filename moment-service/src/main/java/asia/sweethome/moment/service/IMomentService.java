package asia.sweethome.moment.service;

import com.baomidou.mybatisplus.extension.service.IService;

import asia.sweethome.moment.entity.dto.PostMomentDTO;
import asia.sweethome.moment.entity.po.Moment;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-15
 */
public interface IMomentService extends IService<Moment> {

    void postMoment(Long userId, PostMomentDTO dto);
}
