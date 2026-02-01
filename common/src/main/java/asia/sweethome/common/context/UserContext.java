package asia.sweethome.common.context;

/**
 * 【当前登录用户上下文】
 * <p>
 * 网关（gateway）校验完 Token 后，会把用户 ID 放进请求头 X-User-Id 转发给各个业务服务。
 * 业务服务收到请求后，由拦截器把这个 ID 存到这里；之后 Service 层任何地方想知道
 * 「现在是谁在操作」，直接调 {@link #getUserId()} 即可，不用一路把 userId 当参数传下去。
 * <p>
 * 底层用 ThreadLocal 实现：每个请求由一个独立线程处理，ThreadLocal 相当于给这个线程
 * 挂了一个「专属储物柜」，线程之间互不干扰，所以并发请求不会串号。
 * <p>
 * ⚠️ 注意：正因为数据是「绑定在线程上」的，请求处理结束后必须调用 {@link #clear()} 清理，
 * 否则线程被线程池复用给下一个请求时，会读到上一个用户残留的 ID（既是内存泄漏也是安全隐患）。
 * 清理动作已由拦截器在请求结束时统一完成。
 */
public class UserContext {

    // 线程私有的存储槽，每个处理请求的线程各存各的 userId
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    /** 存入当前请求的用户 ID（由拦截器在请求开始时调用） */
    public static void set(Long userId) {
        HOLDER.set(userId);
    }

    /** 取出当前请求的用户 ID；若当前请求未携带用户信息（如未登录接口），返回 null */
    public static Long getUserId() {
        return HOLDER.get();
    }

    /** 清空当前线程的用户 ID（务必在请求结束时调用，防止线程复用导致串号） */
    public static void clear() {
        HOLDER.remove();
    }

}
