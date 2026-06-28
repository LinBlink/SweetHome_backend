package asia.sweethome.moment.entity.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * @author LocrianFifth
 * @since 2026-07-20
 */
@Data
public class QueryPublicMomentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long total;

    private List<PublicMomentVO> moments;

}
