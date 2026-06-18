package asia.sweethome.moment.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/15/2026 7:40 PM
 */

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class MomentLikeDetailsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer totalLikes;

    private List<LikeUserDetailVO> likers;

}