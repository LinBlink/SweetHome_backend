package asia.sweethome.redpacket.controller.v1;


import org.springframework.web.bind.annotation.*;

import java.util.List;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.redpacket.entity.dto.RedpacketDTO;
import asia.sweethome.redpacket.entity.vo.RedpacketVO;
import asia.sweethome.redpacket.service.IRedpacketService;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-21
 */
@Tag(name = "红包控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/redpacket")
public class RedpacketController {

    private final IRedpacketService redpacketService;


    /**
     * 红包创建接口
     * @param dto
     * 需要知道 红包总金额、红包数量，以及该红包属于哪个对话。不可以发红包到不属于该对话的地方。
     */
    @Operation(summary = "创建红包")
    @PostMapping
    public Result<RedpacketVO> createRedpacket(
            @RequestBody
            RedpacketDTO
            dto
    ){

        // 前置校验
        Long conversationId = dto.getConversationId();
        Long totalAmount = dto.getTotalAmount();
        Integer totalCount = dto.getTotalCount();

        if (conversationId == null || totalAmount == null || totalCount == null || totalAmount <= 0 || totalCount <= 0) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR
            );
        }

        // 余额无法被有效均分

        if ( totalAmount < totalCount ) {
            throw new BusinessException(
                    ErrorCode.INVALID_REDPACKET_AMOUNT
            );
        }

        Long userId = UserContext.getUserId();

        // 良好实践：保证只要是传给service的，都必须是有效的参数
        return Result.success(
                BeanUtil.copyProperties(
                        redpacketService.createRedpacket( userId, dto ),
                        RedpacketVO.class
                )
        );
    }

    @Operation(summary = "查询红包详细信息")
    @GetMapping("/{id}")
    public Result<RedpacketVO> getRedpacketDetail(@PathVariable("id") Long redpacketId) {

        if (redpacketId == null) {
            throw new BusinessException(
                    ErrorCode.PARAM_ERROR
            );
        }

        Long userId = UserContext.getUserId();

        return Result.success(
                redpacketService.getRedpacketDetail(userId, redpacketId)
        );
    }

    @Operation(summary = "查看我发出的红包")
    @GetMapping("/i-sent")
    public Result<List<RedpacketVO>> getRedpacketsISent(){

        Long userId = UserContext.getUserId();
        return Result.success(
                redpacketService.getRedpacketsISent( userId )
        );

    }



}
