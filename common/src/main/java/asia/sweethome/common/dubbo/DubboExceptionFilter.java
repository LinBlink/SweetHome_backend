package asia.sweethome.common.dubbo;

import asia.sweethome.common.exception.BusinessException;
import asia.sweethome.common.exception.ErrorCode;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 覆盖 Dubbo 内置的 exception 过滤器（同名 SPI，见 META-INF/dubbo/org.apache.dubbo.rpc.Filter）。
 * <p>
 * 内置过滤器在异常类与服务接口类不在同一个 jar 时（这里 BusinessException 属于 common 模块，
 * 而各 Api 接口属于 api 模块，恰好命中这条规则），会把异常整体替换成一个新的 RuntimeException，
 * 仅保留 toString() 文本，丢失 BusinessException.code —— 导致跨服务调用抛出的业务异常最终都会
 * 在消费方被 GlobalExceptionHandler 当成未知异常返回 500。这里显式放行 BusinessException，
 * 其余未预期异常统一包装为 SYSTEM_ERROR，避免内部实现细节跨服务泄漏。
 */
// @Activate：让这个过滤器对「服务提供方(PROVIDER)」和「服务消费方(CONSUMER)」两端都自动生效
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class DubboExceptionFilter implements Filter, Filter.Listener {

    private static final Logger log = LoggerFactory.getLogger(DubboExceptionFilter.class);

    // 调用「之前」的钩子：这里不做处理，直接放行让真正的方法执行
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }

    // 调用「返回结果之后」的钩子：在这里检查并修正异常
    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        // 没抛异常、或是泛化调用（GenericService），都无需处理
        if (!appResponse.hasException() || GenericService.class == invoker.getInterface()) {
            return;
        }

        Throwable exception = appResponse.getException();

        // 是我们自己的业务异常 → 原样放行，保住它携带的 code（这正是本过滤器存在的核心目的）
        if (exception instanceof BusinessException) {
            return;
        }

        // 未声明的受检异常，包一层业务异常返回
        if (!(exception instanceof RuntimeException) && exception instanceof Exception) {
            log.error("Dubbo 调用出现未预期受检异常 interface={} method={}",
                    invoker.getInterface(), invocation.getMethodName(), exception);
            appResponse.setException(new BusinessException(ErrorCode.SYSTEM_ERROR));
            return;
        }

        // 方法签名 throws 中显式声明过的异常类型，原样传递
        try {
            Method method = invoker.getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
            for (Class<?> declared : method.getExceptionTypes()) {
                if (exception.getClass().equals(declared)) {
                    return;
                }
            }
        } catch (NoSuchMethodException ignored) {
            return;
        }

        log.error("Dubbo 调用出现未预期异常 interface={} method={}",
                invoker.getInterface(), invocation.getMethodName(), exception);
        appResponse.setException(new BusinessException(ErrorCode.SYSTEM_ERROR));
    }

    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        // 网络/框架层异常（非业务方法抛出），交由 Dubbo 默认处理
    }
}
