package asia.sweethome.health.controller.v1;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.health.entity.dto.HealthRecordDTO;
import asia.sweethome.health.entity.vo.HealthRecordVO;
import asia.sweethome.health.service.IHealthRecordsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * @author LocrianFifth
 * @since 2026-07-19
 */
@Tag(name = "成员健康记录控制器")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/health/records")
public class HealthRecordsController {

    private final IHealthRecordsService healthRecordsService;

    @Operation(summary = "提交一条健康记录（同一天同一指标重复提交是覆盖更新）")
    @PostMapping
    public Result<HealthRecordVO> submitRecord(@RequestBody HealthRecordDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        HealthRecordVO vo = healthRecordsService.submitRecord(userId, dto);
        return Result.success(vo);
    }

    @Operation(summary = "手动修改一条已有的健康记录（只能改自己的，metricType 不允许改）")
    @PutMapping("/{recordId}")
    public Result<HealthRecordVO> updateRecord(@PathVariable Long recordId, @RequestBody HealthRecordDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        HealthRecordVO vo = healthRecordsService.updateRecord(userId, recordId, dto);
        return Result.success(vo);
    }

    @Operation(summary = "查自己的健康记录历史")
    @GetMapping
    public Result<List<HealthRecordVO>> queryMyRecords(
            @RequestParam(required = false) String metricType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<HealthRecordVO> records = healthRecordsService.queryMyRecords(userId, metricType, from, to, page, pageSize);
        return Result.success(records);
    }

    @Operation(summary = "查某个家庭成员公开的健康记录")
    @GetMapping("/family/{memberId}")
    public Result<List<HealthRecordVO>> queryFamilyMemberRecords(
            @PathVariable Long memberId,
            @RequestParam(required = false) String metricType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        Long viewerId = UserContext.getUserId();
        if (viewerId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<HealthRecordVO> records = healthRecordsService.queryFamilyMemberRecords(viewerId, memberId, metricType, from, to, page, pageSize);
        return Result.success(records);
    }

}
