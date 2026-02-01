package asia.sweethome.auth.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * Refresh Token 表（支持多设备登录）
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("refresh_tokens")
public class RefreshToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户
     */
    private Long userId;

    /**
     * 实际 Refresh Token 的 SHA-256 哈希（不存明文）
     */
    private String tokenHash;

    /**
     * 设备信息（User-Agent / 设备名）
     */
    private String deviceInfo;

    /**
     * Refresh Token 过期时间
     */
    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    /**
     * 主动吊销时间（登出时设置）
     */
    private LocalDateTime revokedAt;

}
