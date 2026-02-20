package asia.sweethome.family.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 【families 表实体（PO）】
 * <p>
 * 一个家庭一行。邀请码相关的两个字段（inviteCode / inviteExpiresAt）在生成邀请码时才会填。
 *
 * @author LocrianFifth
 * @since 2026-07-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("families")
public class Family implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 家庭名称，如：王家
     */
    private String name;

    /**
     * 家庭 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 软删除时间戳
     */
    private LocalDateTime deletedAt;


}
