package asia.sweethome.api.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * viewer 相对 target 的亲属称谓计算结果；两人不在同一家庭（无关系路径）时 relationCode/relationLabel 均为 null。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String relationCode;
    private String relationLabel;
}
