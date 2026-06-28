package asia.sweethome.health.controller.v1;

import org.springframework.web.bind.annotation.*;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.health.entity.dto.HealthReminderDTO;
import asia.sweethome.health.entity.vo.HealthReminderVO;
import asia.sweethome.health.service.IHealthReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * @author LocrianFifth
 * @since 2026-07-19
 */
@Tag(name = "健康记录提醒控制器")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/health/reminder")
public class HealthReminderController {

    private final IHealthReminderService healthReminderService;

    @Operation(summary = "查自己的提醒设置（从未设置过时 remindTime 为 null）")
    @GetMapping
    public Result<HealthReminderVO> queryMyReminder() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return Result.success(healthReminderService.queryMyReminder(userId));
    }

    @Operation(summary = "修改自己的提醒设置")
    @PutMapping
    public Result<Void> updateReminder(@RequestBody HealthReminderDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        healthReminderService.updateReminder(userId, dto);
        return Result.success();
    }

}
