package asia.sweethome.moment.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

import asia.sweethome.moment.entity.dto.PostCommentDTO;
import asia.sweethome.moment.entity.po.Comment;
import asia.sweethome.moment.entity.vo.CommentVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-15
 */
public interface ICommentService extends IService<Comment> {

    void postComment(Long userId, Long momentId, PostCommentDTO dto);

    List<CommentVO> listComments(Long momentId);

    void deleteComment(Long userId, Long commentId);
}
