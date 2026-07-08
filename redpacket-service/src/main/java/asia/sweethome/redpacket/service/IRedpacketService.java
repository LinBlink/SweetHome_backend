package asia.sweethome.redpacket.service;

import com.baomidou.mybatisplus.extension.service.IService;

import asia.sweethome.redpacket.entity.dto.RedpacketDTO;
import asia.sweethome.redpacket.entity.po.Redpacket;

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
}
