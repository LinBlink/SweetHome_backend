package asia.sweethome.moment.controller.v1;


import org.springframework.web.bind.annotation.*;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.moment.entity.vo.MomentLikeDetailsVO;
import asia.sweethome.moment.service.IMomentLikerService;
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
@Tag(name = "动态点赞控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/moment/liker")
public class MomentLikerController {

    private final IMomentLikerService momentLikerService;

    @PostMapping("/{momentId}")
    public Result<Void> likeMoment( @PathVariable("momentId") Long momentId){

        Long userId = UserContext.getUserId();
        if (userId==null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        momentLikerService.likeMoment(  momentId, userId );

        return Result.success();

    }

    @DeleteMapping("/{momentId}")
    public Result<Void> unlikeMoment( @PathVariable("momentId") Long momentId){

        Long userId = UserContext.getUserId();
        if (userId==null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        momentLikerService.unlikeMoment(  momentId,  userId );

        return Result.success();

    }

    @GetMapping("/{momentId}/like-detail")
    public Result<MomentLikeDetailsVO> getMomentLikeDetails(@PathVariable("momentId") Long momentId){

        Long userId = UserContext.getUserId();
        if (userId==null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        MomentLikeDetailsVO vo =  momentLikerService.getMomentLikeDetails(  momentId );

        return Result.success( vo );

    }

    @GetMapping("/{momentId}/like-count")
    public Result<Integer> getMomentLikeCount(
            @PathVariable("momentId") Long momentId
    ){
        Long userId = UserContext.getUserId();
        if (userId==null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return Result.success(
                momentLikerService.getMomentLikeCount(momentId)
        );

    }


}
