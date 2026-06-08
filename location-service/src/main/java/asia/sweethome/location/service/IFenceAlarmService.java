package asia.sweethome.location.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

import asia.sweethome.location.entity.po.FenceAlarm;
import asia.sweethome.location.entity.ro.CurrentLocationRO;
import asia.sweethome.location.entity.vo.FenceAlarmVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-14
 */
public interface IFenceAlarmService extends IService<FenceAlarm> {

     void checkAndRecordCrossing(Long targetUserId, CurrentLocationRO previous, Double newLng, Double newLat);

     List<FenceAlarmVO> listAlarms(Long userId);

}
