package asia.sweethome.common.config;

import asia.sweethome.common.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 【Web 相关配置】
 * <p>
 * 通过实现 {@link WebMvcConfigurer} 来「定制」Spring MVC 的行为，这里主要是把
 * {@link UserContextInterceptor} 注册进拦截器链，让它对所有请求生效。
 * <p>
 * 本类位于 common 模块，并通过 spring.factories/AutoConfiguration.imports 自动装配，
 * 所以每个引入 common 的业务服务都会自动拥有「解析 X-User-Id」的能力，无需各自配置。
 *
 * @author: LOCRIAN_V
 * @date: 7/1/2026 9:58 下午
 */
@Configuration   // 声明这是一个「配置类」，Spring 启动时会读取里面的 @Bean 定义
public class WebConfig implements WebMvcConfigurer {

    /** 把拦截器交给 Spring 容器管理（成为一个 Bean），方便复用与注入 */
    @Bean
    public UserContextInterceptor userContextInterceptor() {
        return new UserContextInterceptor();
    }

    /**
     * 注册拦截器。addPathPatterns("/**") 表示对「所有路径」的请求都拦截。
     * 加上 @Override 是为了明确表明这是在覆盖父接口的方法，写错方法名时编译器会报错。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor())
                .addPathPatterns("/**");
    }

}
