package asia.sweethome.location.controller.v1;


import org.springframework.web.bind.annotation.*;

import java.util.List;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.location.entity.dto.CreateFenceDTO;
import asia.sweethome.location.entity.vo.FenceVO;
import asia.sweethome.location.service.IFenceService;
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
@RequestMapping("/v1/location/fence")
public class FenceController {

    private final IFenceService fenceService;

    @Operation(summary = "创建围栏")
    @PostMapping
    public Result<Void> createFence(
            @RequestBody
            CreateFenceDTO
            dto
    ){

        Long userId = UserContext.getUserId();

        if (userId==null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        fenceService.createFence(
                userId,
                dto
        );

        return Result.success();
    }

    @Operation(summary = "删除围栏")
    @DeleteMapping("/{fenceId}")
    public Result<Void> deleteFence(
            @PathVariable Long fenceId
    ) {

        Long userId = UserContext.getUserId();

        if (userId==null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        fenceService.deleteFence(userId, fenceId);

        return Result.success();
    }

    @Operation(summary = "查看本家庭的所有围栏")
    @GetMapping
    public Result<List<FenceVO>> listFamilyFences() {

        Long userId = UserContext.getUserId();

        if (userId==null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<FenceVO> vos = fenceService.listFamilyFences(userId);

        return Result.success(vos);
    }

}
