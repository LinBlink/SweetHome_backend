package asia.sweethome.location.controller.v1;

import org.springframework.web.bind.annotation.*;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.location.entity.dto.LocationReportDTO;
import asia.sweethome.location.entity.vo.FamilyMemberLocationsVO;
import asia.sweethome.location.service.ILocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-13
 */
@RequiredArgsConstructor
@RestController
@Tag(name="家庭定位模块相关接口")
@RequestMapping("/v1/location")
public class LocationController {


    private final ILocationService locationService;

    @Operation(summary = "用户报告自身位置")
    @PostMapping("/report")
    public Result<Void> reportLocation(@RequestBody
                                           LocationReportDTO dto
                                       ){
        Long userId = UserContext.getUserId();
        if (userId==null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        locationService.reportLocation( userId, dto  );
        return Result.success();
    }

    @Operation(summary = "获取所有家庭成员的最新位置")
    @GetMapping("/family")
    public Result<FamilyMemberLocationsVO> getFamilyLocations(){

        Long userId = UserContext.getUserId();
        if (userId==null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        FamilyMemberLocationsVO vo = locationService.getFamilyLocations( userId  );
        return Result.success( vo );
    }




}
