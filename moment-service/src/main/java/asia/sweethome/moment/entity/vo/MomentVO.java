package asia.sweethome.moment.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/15/2026 4:15 PM
 */

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class MomentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String username;

    private String userAvatarUrl;

    private LocalDateTime createdAt;

    private String content;

    private List<MomentMediaVO> mediaFiles;


}
