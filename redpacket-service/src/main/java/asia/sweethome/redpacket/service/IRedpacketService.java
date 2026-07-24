package asia.sweethome.redpacket.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

import asia.sweethome.redpacket.entity.dto.RedpacketDTO;
import asia.sweethome.redpacket.entity.po.Redpacket;
import asia.sweethome.redpacket.entity.vo.RedpacketGrabVO;
import asia.sweethome.redpacket.entity.vo.RedpacketVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-21
 */
public interface IRedpacketService extends IService<Redpacket> {

    void markRedpacketAsExpired( Long redpacketId );


    Redpacket createRedpacket(Long userId, RedpacketDTO dto);

    RedpacketVO getRedpacketDetail(Long userId, Long redpacketId);

    List<RedpacketVO> getRedpacketsISent(Long userId);

}
