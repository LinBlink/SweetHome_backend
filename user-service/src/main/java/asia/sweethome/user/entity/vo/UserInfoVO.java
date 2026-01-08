package asia.sweethome.user.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @description: 返回当前用户个人信息
 * @author: LOCRIAN_V
 * @date: 6/30/2026 2:27 PM
 */
@Data
public class UserInfoVO implements Serializable {

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
