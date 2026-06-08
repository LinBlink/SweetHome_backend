package asia.sweethome.moment.controller.v1;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.moment.entity.dto.PostMomentDTO;
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

        momentService.postMoment(
                userId, dto
        );

        return Result.success();
    }

}
