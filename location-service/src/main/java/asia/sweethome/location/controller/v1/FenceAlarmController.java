package asia.sweethome.location.controller.v1;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.location.entity.vo.FenceAlarmVO;
import asia.sweethome.location.service.IFenceAlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-14
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "位置分享相关接口")
@RequestMapping("/v1/location/fence-alarm")
public class FenceAlarmController {

    private final IFenceAlarmService fenceAlarmService;

    @Operation(summary = "查看我收到的所有历史报警")
    @GetMapping
    public Result<List<FenceAlarmVO>> listAlarms() {

        Long userId = UserContext.getUserId();

        if (userId==null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<FenceAlarmVO> vos = fenceAlarmService.listAlarms(userId);

        return Result.success(vos);
    }

}
