package asia.sweethome.redpacket.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

import asia.sweethome.redpacket.entity.po.RedpacketGrab;
import asia.sweethome.redpacket.entity.vo.RedpacketGrabVO;

/**
 * <p>
 * 记录每个红包每个人抢的情况，红包-用户：1-N 服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-21
 */
public interface IRedpacketGrabsService extends IService<RedpacketGrab> {


    RedpacketGrab grabRedpacket( Long userId , Long redpacketId );


    List<RedpacketGrabVO> getRedpacketGrabDetail(Long userId, Long redpacketId);

    void persistGrab(Long redpacketId, Long userId, Long amount);


    List<RedpacketGrabVO> getRedpacketsIGrabbed(Long userId);
}
