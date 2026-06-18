package asia.sweethome.user.controller.v1;


import org.springframework.web.bind.annotation.*;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.user.entity.dto.PushTokenDTO;
import asia.sweethome.user.service.IPushTokensService;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-14
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/push-token")
public class PushTokensController {

    private final IPushTokensService pushTokensService;

    @PostMapping
    public Result<Void> pushToken(
            @RequestBody
            PushTokenDTO
                    dto
    ) {
        // dto 校验，AI写

        Long userId = UserContext.getUserId();

        if (userId == null) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED
            );
        }

        pushTokensService.pushToken(
                userId,
                dto
        );

        return Result.success();

    }

    @DeleteMapping
    public Result<Void> deleteToken(
            @RequestBody
            PushTokenDTO
                    dto
    ) {
        // dto 校验，AI写

        Long userId = UserContext.getUserId();

        if (userId == null) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED
            );
        }

        pushTokensService.deleteToken(
                userId,
                dto
        );

        return Result.success();

    }

}
