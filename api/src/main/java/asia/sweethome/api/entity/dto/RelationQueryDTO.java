package asia.sweethome.api.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 查询 viewer 相对 target 的亲属称谓（见 doc/api.md 「七、亲属称谓计算算法」）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long viewerUserId;     // 观察者：站在谁的视角来称呼（「我」）
    private Long targetUserId;     // 目标：要称呼的是谁（「对方」）
    private String acceptLanguage; // 语言偏好（HTTP Accept-Language），决定称谓用哪种语言/方言
}
