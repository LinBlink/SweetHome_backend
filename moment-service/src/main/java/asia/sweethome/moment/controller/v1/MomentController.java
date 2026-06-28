package asia.sweethome.moment.controller.v1;


import org.springframework.web.bind.annotation.*;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.moment.entity.dto.PostMomentDTO;
import asia.sweethome.moment.entity.vo.QueryMyFamilyMomentVO;
import asia.sweethome.moment.entity.vo.QueryPublicMomentVO;
import asia.sweethome.moment.service.IMomentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-15
 */
@Tag(name = "用户动态控制器")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/moment")
public class MomentController {

    private final IMomentService momentService;



    @Operation(summary = "发布动态")
    @PostMapping
    public Result<Void> postMoment(
            @RequestBody
            PostMomentDTO
            dto
    ){
        Long userId = UserContext.getUserId();

        if (userId == null) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED
            );
        }

        momentService.postMoment(
                userId, dto
        );

        return Result.success();
    }

    @Operation(summary = "查询自己家庭的动态")
    @GetMapping("/myfamily")
    public Result<QueryMyFamilyMomentVO> queryMyFamilyMoment(
            @RequestParam(value = "page" , required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "asc", defaultValue = "false") Boolean asc
    ) {

        Long userId = UserContext.getUserId();

        if (userId == null) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED
            );
        }

        QueryMyFamilyMomentVO vo = momentService.queryMyFamilyMoment(
                userId, page, pageSize, asc
        );

        return Result.success(vo);

    }

    @Operation(summary = "查看动态广场（跨家庭公开动态）")
    @GetMapping("/public")
    public Result<QueryPublicMomentVO> queryPublicMoment(
            @RequestParam(value = "page" , required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "asc", defaultValue = "false") Boolean asc
    ) {

        Long userId = UserContext.getUserId();

        if (userId == null) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED
            );
        }

        QueryPublicMomentVO vo = momentService.queryPublicMoment(
                page, pageSize, asc
        );

        return Result.success(vo);

    }

    @Operation(summary = "删除自己发布的动态")
    @DeleteMapping("/{momentId}")
    public Result<Void> deleteMoment(
            @PathVariable Long momentId
    ){
        Long userId = UserContext.getUserId();

        if (userId == null) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED
            );
        }


        momentService.deleteMoment(
                userId,
                momentId
        );


        return Result.success();

    }

}
