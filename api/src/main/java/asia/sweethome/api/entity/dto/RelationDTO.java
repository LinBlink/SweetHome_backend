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

    private String relationCode;  // 关系编码（程序内部用），如 "FATHER"
    private String relationLabel; // 关系称谓（给人看），如「爸爸」
}
