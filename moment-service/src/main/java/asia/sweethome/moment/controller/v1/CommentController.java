package asia.sweethome.moment.controller.v1;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import asia.sweethome.common.context.UserContext;
import asia.sweethome.common.entity.vo.Result;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.moment.entity.dto.PostCommentDTO;
import asia.sweethome.moment.entity.vo.CommentVO;
import asia.sweethome.moment.service.ICommentService;
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
@Tag(name = "动态评论控制器")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/moment/comment")
public class CommentController {

    private final ICommentService commentService;

    @Operation(summary = "发表评论")
    @PostMapping("/{momentId}")
    public Result<Void> postComment(
            @PathVariable("momentId") Long momentId,
            @RequestBody PostCommentDTO dto
    ) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        commentService.postComment(userId, momentId, dto);

        return Result.success();
    }

    @Operation(summary = "查看某条动态的所有评论")
    @GetMapping("/{momentId}")
    public Result<List<CommentVO>> listComments(
            @PathVariable("momentId") Long momentId
    ) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return Result.success(commentService.listComments(momentId));
    }

    @Operation(summary = "删除自己发布的评论")
    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(
            @PathVariable("commentId") Long commentId
    ) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        commentService.deleteComment(userId, commentId);

        return Result.success();
    }

}
