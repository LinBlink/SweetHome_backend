package asia.sweethome.redpacket.entity.vo;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/23/2026 2:17 下午
 */

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RedpacketGrabVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 抢到的用户 userId
     */
    private Long userId;

    /**
     * 抢到的用户 username
     */
    private String username;


    /**
     * 抢到的用户 userAvatarUrl
     */
    private String userAvatarUrl;

    /**
     * 抢到的红包id
     */
    private Long id;

    /**
     * 红包id
     */
    private Long redpacketId;

    /**
     * 红包id
     */
    private Long redpacketOwnerId;

    /**
     * 红包所有者用户名
     */
    private String redpacketOwnerUsername;

    /**
     * 红包所有者头像
     */
    private String redpacketOwnerUserAvatarUrl;

    private Long grabAmount;

    private LocalDateTime createdAt;

}