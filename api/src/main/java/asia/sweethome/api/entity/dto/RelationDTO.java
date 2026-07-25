package asia.sweethome.api.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * viewer 相对 target 的亲属称谓计算结果；两人不在同一家庭（无关系路径）时 relationCode 为 null。
 * 只返回语言无关的关系编码，可读称谓由前端拿 relationCode 自行本地化（见 doc/API.md 11.5/11.6）。
 * <p>
 * 除 relationCode 外还必须带上两边的性别，因为<b>光有编码不足以确定称谓</b>——API.md 11.6 里
 * 「以及消歧所需的 gender」说的就是这件事，只是一直没实现。两处需要：
 * <ul>
 *   <li><b>targetGender</b>：编码 {@code S} 只说明「是配偶」，说不清是丈夫还是妻子；</li>
 *   <li><b>viewerGender</b>：{@code S.F} 在中文里，男方叫岳父、女方叫公公——同一个编码两个词。
 *       前端 kinship_localizer 用 {@code <code>#<male|female>} 形式的键来区分，共 8 个这类编码
 *       （eB / eZ / S.F / S.M / S.eB / S.yB / S.eZ / S.yZ）。</li>
 * </ul>
 * ⚠️ 调用方必须原样透传这两个值，<b>不要在拿到 null 时兜底成 male</b>——猜错性别就是当着用户
 * 的面把称谓叫错，宁可让前端显示中性文案。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String relationCode;  // 关系编码，如 "F.F"；前端据此本地化为称谓
    private String viewerGender;  // 观察者性别 male/female，消歧 S.F 这类「随我方性别而变」的称谓
    private String targetGender;  // 目标性别 male/female，消歧 S（丈夫/妻子）

    /** 只有编码、拿不到性别时用（例如两人不在同一家庭，压根没有称谓可言） */
    public RelationDTO(String relationCode) {
        this.relationCode = relationCode;
    }
}
