package asia.sweethome.common.config;

import asia.sweethome.common.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 【全局异常处理器的自动装配】
 * <p>
 * 让每个引入 common 的业务服务，都自动拥有一个 {@link GlobalExceptionHandler}，
 * 无需在各服务里重复 new 一遍。
 * <p>
 * {@code @ConditionalOnMissingBean}：只有当容器里「还没有」GlobalExceptionHandler 时才创建这个默认的。
 * 也就是说——如果某个服务想自定义自己的全局异常处理，只要自己声明一个同类型 Bean，
 * 就会「顶替」掉这里的默认实现（这就是原注释「微服务有了就别用了」的意思）。
 *
 * @author: LOCRIAN_V
 * @date: 7/2/2026 4:20 下午
 */
@Configuration
public class ExceptionConfig {

    @Bean
    @ConditionalOnMissingBean   // 服务自己没定义时，才用这个默认的兜底处理器
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

}
