package asia.sweethome.redpacket.controller.v1;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.redpacket.entity.dto.RedPacketGrabDTO;
import asia.sweethome.redpacket.entity.po.RedpacketGrab;
import asia.sweethome.redpacket.service.IRedpacketGrabsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 记录每个红包每个人抢的情况，红包-用户：1-N 前端控制器
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-21
 */
@Tag(name = "抢红包相关接口")
@RestController
@RequestMapping("/v1/redpacket-grabs")
@RequiredArgsConstructor
public class RedpacketGrabsController {

    private final IRedpacketGrabsService redpacketGrabsService;

    @Operation(summary = "抢红包")
    @PostMapping
    public Result<RedpacketGrab> grabRedpacket(
            @RequestBody
            RedPacketGrabDTO
            dto
    ){

        Long redpacketId = dto.getRedpacketId();
        Long userId = UserContext.getUserId();

        if (redpacketId == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR
            );
        }

        RedpacketGrab redpacketGrab = redpacketGrabsService.grabRedpacket(
                userId, redpacketId
        );

        return Result.success(redpacketGrab);

    }

}
