package asia.sweethome.user.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 【users 表实体（PO）】
 * <p>
 * 对应数据库 users 表，一个 User 对象 = 表里一行。字段与列一一对应，
 * MyBatis-Plus 靠 @TableName / @TableId 建立映射关系。
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
     * 用户 ID（主键，数据库自增）
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

    /**
     * 用户钱包余额
     */
    private Long balance;

}
