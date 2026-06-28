package asia.sweethome.moment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import asia.sweethome.api.UserApi;
import asia.sweethome.api.entity.dto.UserDTO;
import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import asia.sweethome.moment.entity.dto.PostCommentDTO;
import asia.sweethome.moment.entity.po.Comment;
import asia.sweethome.moment.entity.po.Moment;
import asia.sweethome.moment.entity.vo.CommentVO;
import asia.sweethome.moment.mapper.CommentMapper;
import asia.sweethome.moment.service.ICommentService;
import asia.sweethome.moment.service.IMomentService;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-15
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @DubboReference
    private UserApi userApi;

    private final IMomentService momentService;

    @Override
    public void postComment(Long userId, Long momentId, PostCommentDTO dto) {

        if (dto == null || StrUtil.isBlank(dto.getContent())) {
            throw new BusinessException(ErrorCode.COMMENT_CONTENT_EMPTY);
        }

        // 注：moment 表的 deleted_at 目前没有配 @TableLogic，deleteMoment 走的是物理删除，
        // 所以这里只用判空就够了，不需要再判 deletedAt（判了也是死代码，永远不会命中）
        Moment moment = momentService.getById(momentId);
        if (moment == null) {
            throw new BusinessException(ErrorCode.NO_SUCH_MOMENT);
        }

        LocalDateTime now = LocalDateTime.now();

        Comment comment = new Comment();
        comment.setMomentId(momentId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        // 冗余拷贝父动态的公开状态，评论广场按 comment.is_public 过滤时不用 join moment 表
        comment.setIsPublic(Boolean.TRUE.equals(moment.getIsPublic()));
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        save(comment);

    }

    @Override
    public List<CommentVO> listComments(Long momentId) {

        List<Comment> comments = lambdaQuery()
                .eq(Comment::getMomentId, momentId)
                .orderByAsc(Comment::getCreatedAt)
                .list();

        if (comments.isEmpty()) {
            return List.of();
        }

        // 批量查评论者的用户信息，避免循环里一个个查 UserApi
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, UserDTO> userIdUserDTOMap = userApi.findUsersByIds(userIds).stream()
                .collect(Collectors.toMap(UserDTO::getId, userDTO -> userDTO));

        return comments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setUserId(comment.getUserId());
            vo.setContent(comment.getContent());
            vo.setCreatedAt(comment.getCreatedAt());

            UserDTO userDTO = userIdUserDTOMap.get(comment.getUserId());
            if (userDTO != null) {
                vo.setUsername(userDTO.getName());
                vo.setUserAvatarUrl(userDTO.getAvatarUrl());
            }

            return vo;
        }).collect(Collectors.toList());

    }

    @Override
    public void deleteComment(Long userId, Long commentId) {

        Comment comment = getById(commentId);

        if (comment == null) {
            throw new BusinessException(ErrorCode.NO_SUCH_COMMENT);
        }

        if (!userId.equals(comment.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_COMMENT_OWNER);
        }

        removeById(commentId);

    }

}
