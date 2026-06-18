package asia.sweethome.moment.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/15/2026 4:16 PM
 */

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MomentMediaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;

    private String content;

    private LocalDateTime createdAt;

}
