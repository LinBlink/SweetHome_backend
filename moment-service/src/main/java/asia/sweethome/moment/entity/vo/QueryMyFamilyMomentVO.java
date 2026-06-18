package asia.sweethome.moment.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/15/2026 4:03 PM
 */

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class QueryMyFamilyMomentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long total;

    private List<MomentVO> moments;

}
