package asia.sweethome.health.controller.v1;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.health.entity.dto.HealthVisibilityDTO;
import asia.sweethome.health.entity.vo.HealthVisibilityVO;
import asia.sweethome.health.service.IHealthVisibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * @author LocrianFifth
 * @since 2026-07-19
 */
@Tag(name = "健康指标可见性控制器")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/health/visibility")
public class HealthVisibilityController {

    private final IHealthVisibilityService healthVisibilityService;

    @Operation(summary = "查自己的三种指标可见性设置（永远补全返回）")
    @GetMapping
    public Result<List<HealthVisibilityVO>> queryMyVisibility() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return Result.success(healthVisibilityService.queryMyVisibility(userId));
    }

    @Operation(summary = "修改某个指标的可见性")
    @PutMapping
    public Result<Void> updateVisibility(@RequestBody HealthVisibilityDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        healthVisibilityService.updateVisibility(userId, dto);
        return Result.success();
    }

}
