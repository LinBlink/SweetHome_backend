package asia.sweethome.moment.entity.dto;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/15/2026 8:30 PM
 */

import java.io.Serializable;

import lombok.Data;

@Data
public class PostCommentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String content;

}
