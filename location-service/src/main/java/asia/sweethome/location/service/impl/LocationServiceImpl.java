package asia.sweethome.location.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import asia.sweethome.api.FamilyApi;
import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.FamilyDTO;
import asia.sweethome.api.entity.dto.FamilyMemberDTO;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.location.entity.dto.LocationReportDTO;
import asia.sweethome.location.entity.po.Location;
import asia.sweethome.location.entity.ro.CurrentLocationRO;
import asia.sweethome.location.entity.vo.FamilyMemberLocationVO;
import asia.sweethome.location.entity.vo.FamilyMemberLocationsVO;
import asia.sweethome.location.mapper.LocationMapper;
import asia.sweethome.location.registry.CurrentLocationRegistry;
import asia.sweethome.location.service.ILocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-13
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location> implements ILocationService {

    @DubboReference
    private FamilyApi familyApi;

    @DubboReference
    private UserApi userApi;

    private final CurrentLocationRegistry currentLocationRegistry;



    @Override
    public void reportLocation(Long userId, LocationReportDTO dto) {

        Double lng = dto.getLng();
        Double lat = dto.getLat();

        // 经纬度校验

        if (lng==null || lat==null) {
            throw new BusinessException(ErrorCode.LOCATION_COORDINATE_INVALID);
        }

        int battery = dto.getBattery() == null ? -1 : dto.getBattery() ;

        if (battery>100) {
            throw new BusinessException(ErrorCode.LOCATION_BATTERY_INVALID);
        }

        // 时间校验

        LocalDateTime updateTime = dto.getUpdateTime();

        if (updateTime == null) {
            throw new BusinessException(ErrorCode.LOCATION_TIMESTAMP_MISSING);
        }

        LocalDateTime now = LocalDateTime.now();

        Duration duration = Duration.between(updateTime, now);

        // 如果时间差大于120秒，则报警做日志
        if (Math.abs( duration.getSeconds() ) > 120) {
            log.warn("定位上报时间戳偏差过大：userId={}, updateTime={}, now={}, 偏差秒数={}",
                    userId, updateTime, now, duration.getSeconds());
        }

        // 如果时间差大于10分钟，无效处理
        if (Math.abs( duration.getSeconds()) > 600) {
            throw new BusinessException(ErrorCode.LOCATION_TIMESTAMP_STALE);
        }

        // FamilyApi 契约：非家庭成员时直接抛 NO_SUCH_FAMILY_MEMBER（经 Dubbo 透传），不会返回 null，故这里无需再判空
        Long familyId = familyApi.getFamilyByUserId(userId).getId();

        // 入 redis

        CurrentLocationRO currentLocationRO = new CurrentLocationRO();
        currentLocationRO.setUserId(userId);
        currentLocationRO.setFamilyId(familyId);
        currentLocationRO.setLng(lng);
        currentLocationRO.setLat(lat);
        currentLocationRO.setBattery(battery);
        currentLocationRO.setUpdatedAt(updateTime);

        currentLocationRegistry.updateCurrent(
                userId,
                currentLocationRO
        );

        // 入库

        Location location = new Location();
        location.setUserId(userId);
        location.setFamilyId(familyId);
        location.setLng(lng);
        location.setLat(lat);
        location.setBattery(battery);
        location.setUpdatedAt(updateTime);

        save( location );

    }

    @Override
    public FamilyMemberLocationsVO getFamilyLocations(Long userId) {

        FamilyDTO familyByUserId = familyApi.getFamilyByUserId(userId);

        String familyName = familyByUserId.getName();

        Long familyId = familyByUserId.getId();

        FamilyMemberLocationsVO vo = new FamilyMemberLocationsVO();
        vo.setFamilyId( familyId );
        vo.setFamilyName(familyName);

        // 家庭总人数 在线成员数
        //      注意是定位在线，而非设备在线
        List<FamilyMemberDTO> familyMembers = familyApi.getFamilyMembersByFamilyId(familyId);

        Integer onlineCount = 0;

        vo.setTotalMemberCount(
                familyMembers.size()
        );

        List<FamilyMemberLocationVO> familyMemberLocationVOS = new ArrayList<>(familyMembers.size());

        for (FamilyMemberDTO familyMember : familyMembers) {

            CurrentLocationRO currentLocationRO = currentLocationRegistry.getCurrent(
                    familyMember.getUserId()
            );

            if (currentLocationRO == null) {
                continue;
            }

            onlineCount++;

            FamilyMemberLocationVO familyMemberLocationVO = new FamilyMemberLocationVO();
            familyMemberLocationVO.setUserId(familyMember.getUserId());

            // todo redis缓存。需要大改，此处暂时不要帮我写代码。
            UserDTO user = userApi.findUserById(
                    familyMember.getUserId()
            );

            familyMemberLocationVO.setUsername(user.getName());
            familyMemberLocationVO.setUserAvatarUrl(user.getAvatarUrl());


            familyMemberLocationVO.setLng(
                    currentLocationRO.getLng()
            );
            familyMemberLocationVO.setLat(
                    currentLocationRO.getLat()
            );
            familyMemberLocationVO.setBattery(
                    currentLocationRO.getBattery()
            );
            familyMemberLocationVO.setUpdatedAt(
                    currentLocationRO.getUpdatedAt()
            );

            familyMemberLocationVOS.add(
                    familyMemberLocationVO
            );

        }

        vo.setOnlineMemberCount( onlineCount );

        vo.setFamilyMemberLocations( familyMemberLocationVOS );

        return vo;
    }
}
