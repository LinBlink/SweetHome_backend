package asia.sweethome.api.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * viewer 相对 target 的亲属称谓计算结果；两人不在同一家庭（无关系路径）时 relationCode 为 null。
 * 只返回关系编码，可读称谓由前端拿 relationCode 自行本地化（见 doc/API.md 的 code 对照表）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String relationCode;  // 关系编码，如 "F.F"；前端据此本地化为称谓
}
