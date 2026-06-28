package asia.sweethome.common.constants;

/**
 * 【跨服务共用的 Kafka topic 名】
 * <p>
 * topic 名字符串本身就是「生产者、消费者必须达成一致的契约」——写在各自服务里各定义一份，
 * 一旦拼写不一致（比如一边 "fence.alarm.triggered"，一边少个点），消费者永远收不到消息，
 * 还不容易排查。所以跟 ErrorCode、DTO 一样，把它放进 common 这个共享内核，两边 import 同一个常量。
 *
 * @author: LOCRIAN_V
 * @date: 7/16/2026
 */
public class KafkaTopicConstants {

    /**
     * 电子围栏越界事件：location-service（生产者）检测到越界后发布，
     * user-service（消费者）订阅后查该用户的推送 token 并发送通知。
     * 两边通过 Kafka 解耦，location-service 不需要知道"如何推送"，
     * 上报接口（reportLocation）的响应时间也不会被推送逻辑拖慢。
     */
    public static final String TOPIC_FENCE_ALARM_TRIGGERED = "fence.alarm.triggered";

    public static final String TOPIC_USER_PROFILE_CHANGED = "user.profile.changed";

    public static final String TOPIC_CHAT_MESSAGE_OFFLINE = "chat.message.offline";

    /**
     * 健康记录提醒事件：health-service（生产者）的定时任务扫描到「到点未记录」的成员后发布，
     * user-service（消费者）订阅后复用 PushTokensService 查推送 token 并发送极光通知。
     */
    public static final String TOPIC_HEALTH_REMINDER_TRIGGERED = "health.reminder.triggered";

}
