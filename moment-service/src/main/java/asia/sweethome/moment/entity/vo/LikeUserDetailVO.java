package asia.sweethome.moment.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/15/2026 7:42 PM
 */

import java.io.Serializable;

import lombok.Data;

@Data
public class LikeUserDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private String userAvatarUrl;

    private Integer likeCount;

}