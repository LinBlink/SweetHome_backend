package asia.sweethome.chat.ws;

/**
 * 【WebSocket 连接属性的键名常量】
 * <p>
 * 握手时把身份/语言存进 session.getAttributes()（一个 Map），存取都用这里的常量作 key，
 * 避免两处硬编码字符串写得不一致导致取不到值。私有构造方法防止被 new（纯常量类）。
 */
public final class WsSessionAttributes {
    public static final String USER_ID = "userId";              // 存当前连接的用户 id
    public static final String ACCEPT_LANGUAGE = "acceptLanguage"; // 存语言偏好

    private WsSessionAttributes() {
    }
}