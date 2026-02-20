package asia.sweethome.family.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 【邀请码（对外展示）】生成邀请码接口的返回体：邀请码本身 + 何时过期。
 */
@Data
@AllArgsConstructor
public class InviteCodeVO {
    private String inviteCode;       // 邀请码
    private LocalDateTime expiresAt; // 过期时间
}
