package asia.sweethome.family.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InviteCodeVO {
    private String inviteCode;
    private LocalDateTime expiresAt;
}
