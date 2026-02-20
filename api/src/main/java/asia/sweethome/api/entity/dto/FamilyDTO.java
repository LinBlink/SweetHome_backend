package asia.sweethome.api.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 【家庭信息（跨服务返回值）】
 * <p>
 * family-service 查询/创建/加入家庭后，返回给调用方的家庭数据。字段与数据库 families 表对应。
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
