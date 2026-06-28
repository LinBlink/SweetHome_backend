package asia.sweethome.common.entity.ko;

import java.io.Serializable;

import lombok.Data;

/**
 * 健康记录提醒事件，health-service 发给 user-service 触发极光推送
 *
 * @author: LOCRIAN_V
 * @date: 7/19/2026
 */
@Data
public class HealthReminderNotifyKO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    /** 推送标题 */
    private String title;

    /** 推送内容 */
    private String content;

}
