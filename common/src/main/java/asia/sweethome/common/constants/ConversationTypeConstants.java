package asia.sweethome.common.constants;

/**
 * 【会话类型】
 * <p>
 * 聊天会话分两种，与数据库 conversations 表 type 字段取值一致。
 */
public class ConversationTypeConstants {
    public static final String GROUP = "group";    // 群聊（如整个家庭群）
    public static final String DIRECT = "direct";  // 单聊（两个人一对一）
}
