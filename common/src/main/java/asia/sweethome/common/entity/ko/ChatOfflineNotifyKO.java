package asia.sweethome.common.entity.ko;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/18/2026 12:08 上午
 */

import java.io.Serializable;

import lombok.Data;

/**
 * 发消息给 user-service 进行推送
 */
@Data
public class ChatOfflineNotifyKO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long senderUserId;

    /**
     * 消息发送方用户名
     */
    private String senderUsername;

    /**
     * 发送方头像
     */
    private String senderUserAvatarUrl;

    private Long receiverUserId;

    /**
     * 会话标题。如果是单人，就是 senderUsername，如果是群组，就是群组name
     */
    private String conversationTitle;

    /* 发送的消息内容 */
    private String messageContent;


}
