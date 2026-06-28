package asia.sweethome.moment.entity.dto;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/15/2026 3:14 PM
 */

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class PostMomentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String content;

    private List<MomentMediaDTO> media;

    private Boolean isPublic;

}