package asia.sweethome.moment.entity.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 跨家庭「动态广场」列表项，比 {@link MomentVO} 多 familyId/familyName——
 * 同一家庭内浏览时「是谁的家庭」是废话，跨家庭广场里才是必要的上下文。
 *
 * @author LocrianFifth
 * @since 2026-07-20
 */
@Data
public class PublicMomentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String username;

    private String userAvatarUrl;

    private Long familyId;

    private String familyName;

    private LocalDateTime createdAt;

    private String content;

    private List<MomentMediaVO> mediaFiles;

}
