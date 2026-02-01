package asia.sweethome.api.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 家庭表
 * </p>
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
@Data
public class FamilyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 家庭名称，如：王家
     */
    private String name;

    /**
     * 家庭 ID
     */
    private Long id;

    /**
     * 8位邀请码（大写字母+数字）
     */
    private String inviteCode;

    /**
     * 邀请码过期时间
     */
    private LocalDateTime inviteExpiresAt;

    /**
     * 创建者 user_id（家庭管理员）
     */
    private Long createdBy;

}
