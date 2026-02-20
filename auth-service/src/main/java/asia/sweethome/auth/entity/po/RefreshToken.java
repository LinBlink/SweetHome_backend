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
 * 【refresh_tokens 表实体（PO）】
 * <p>
 * PO = Persistent Object，与数据库表一一对应，一个对象就是表里的一行。
 * 支持多设备登录：同一用户在手机、平板各登录一次，就会有多条记录（deviceInfo 区分）。
 * <ul>
 *   <li>{@code @TableName}：指明对应的表名；</li>
 *   <li>{@code @Accessors(chain = true)}：让 setter 返回自身，可以 a.setX().setY() 链式调用；</li>
 *   <li>{@code @EqualsAndHashCode(callSuper = false)}：equals/hashCode 不考虑父类字段。</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("refresh_tokens")
public class RefreshToken implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键，type = AUTO 表示由数据库自增生成
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
