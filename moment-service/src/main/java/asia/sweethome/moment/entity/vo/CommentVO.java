package asia.sweethome.moment.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/15/2026 8:32 PM
 */

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String username;

    private String userAvatarUrl;

    private String content;

    private LocalDateTime createdAt;

}
