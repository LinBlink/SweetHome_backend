package asia.sweethome.user.entity.po;

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
 * 用户表
 * </p>
 *
 * @author author
 * @since 2026-06-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 手机号（唯一登录凭证）
     */
    private String phone;

    /**
     * BCrypt 加密后的密码
     */
    private String passwordHash;

    /**
     * 用户昵称
     */
    private String name;

    /**
     * 头像 OSS 地址
     */
    private String avatarUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 软删除时间戳，NULL 表示未删除
     */
    private LocalDateTime deletedAt;


}
