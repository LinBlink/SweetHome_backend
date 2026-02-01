package asia.sweethome.common.constants;

/**
 * 【消息类型】
 * <p>
 * messages.type 取值（与 doc/schema.sql 的 ENUM 一致，均为小写）。
 */
public class MessageTypeConstants {
    public static final String TEXT = "text";     // 文字消息
    public static final String IMAGE = "image";   // 图片消息
    public static final String VOICE = "voice";   // 语音消息
    public static final String SYSTEM = "system"; // 系统消息（如「XX 加入了家庭」）
}
