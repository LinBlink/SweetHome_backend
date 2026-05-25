package asia.sweethome.location.service;

import com.baomidou.mybatisplus.extension.service.IService;

import asia.sweethome.location.entity.dto.LocationReportDTO;
import asia.sweethome.location.entity.po.Location;
import asia.sweethome.location.entity.vo.FamilyMemberLocationsVO;

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
}
