package asia.sweethome.moment.entity.dto;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/15/2026 3:18 PM
 */

import java.io.Serializable;

import lombok.Data;

@Data
public class MomentMediaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 媒体类型
     */
    private String type;

    /**
     * 媒体链接
     */
    private String content;

}
