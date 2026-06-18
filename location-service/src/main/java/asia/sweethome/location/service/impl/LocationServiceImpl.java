package asia.sweethome.location.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

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
import asia.sweethome.location.entity.vo.LocationPointVO;
import asia.sweethome.location.entity.vo.UserLocationHistoryVO;
import asia.sweethome.location.mapper.LocationMapper;
import asia.sweethome.location.registry.CurrentLocationRegistry;
import asia.sweethome.location.service.IFenceAlarmService;
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

    private final IFenceAlarmService fenceAlarmService;


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

        LocalDateTime updateTime = LocalDateTime.now();

        if (updateTime == null) {
            throw new BusinessException(ErrorCode.LOCATION_TIMESTAMP_MISSING);
        }

        // todo 前端发送时间校验？

        // redis 取数据
        CurrentLocationRO current = currentLocationRegistry.getCurrent(userId);

        // ---------------------------------------------
        // 越界校验
        fenceAlarmService.checkAndRecordCrossing(
                userId,
                current,
                lng,
                lat
        );


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

    @Override
    public UserLocationHistoryVO getUserLocationHistoryBetween(Long userId, Long targetUserId, LocalDateTime dayStart, LocalDateTime dayEnd) {

        FamilyDTO currentUserFamily = familyApi.getFamilyByUserId(userId);
        FamilyDTO targetFamily = familyApi.getFamilyByUserId(targetUserId);
        UserDTO targetUser = userApi.findUserById(targetUserId);

        if (!currentUserFamily.getId().equals(targetFamily.getId())) {
            throw new BusinessException(ErrorCode.LOCATION_TARGET_NOT_FAMILY_MEMBER);
        }

        List<Location> locations = lambdaQuery().eq(
                        Location::getUserId, targetUserId  // 查询的target
                ).eq(
                        Location::getFamilyId, currentUserFamily.getId() // 保证查询对方和自己在同一个家庭
                )
                .ge(
                        Location::getUpdatedAt, dayStart
                ).le(
                        Location::getUpdatedAt, dayEnd
                ).orderByAsc(
                        Location::getUpdatedAt
                ).list();

        UserLocationHistoryVO vo = new UserLocationHistoryVO();
        vo.setFamilyId(targetFamily.getId());
        vo.setFamilyName(targetFamily.getName());
        vo.setUserId(targetUserId);
        vo.setUsername(targetUser.getName());
        vo.setUserAvatarUrl(targetUser.getAvatarUrl());

        List<LocationPointVO> locationPointVOS = new ArrayList<>(locations.size());

        for (Location location : locations) {

            LocationPointVO locationPointVO = new LocationPointVO();
            locationPointVO.setLng(location.getLng());
            locationPointVO.setLat(location.getLat());
            locationPointVO.setBattery(location.getBattery());
            locationPointVO.setUpdatedAt(location.getUpdatedAt());

            locationPointVOS.add(locationPointVO);

        }

        vo.setLocations(locationPointVOS);


        return vo;
    }

}
