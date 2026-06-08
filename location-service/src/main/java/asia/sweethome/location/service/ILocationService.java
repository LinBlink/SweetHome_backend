package asia.sweethome.location.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

import asia.sweethome.location.entity.dto.LocationReportDTO;
import asia.sweethome.location.entity.po.Location;
import asia.sweethome.location.entity.vo.FamilyMemberLocationsVO;
import asia.sweethome.location.entity.vo.UserLocationHistoryVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-13
 */
public interface ILocationService extends IService<Location> {

    void reportLocation(Long userId, LocationReportDTO dto);

    FamilyMemberLocationsVO getFamilyLocations(Long userId);

    UserLocationHistoryVO getUserLocationHistoryBetween(Long userId, Long targetUserId, LocalDateTime dayStart, LocalDateTime dayEnd);

}
