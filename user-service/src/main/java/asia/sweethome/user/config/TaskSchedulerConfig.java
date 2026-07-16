package asia.sweethome.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @description:
 * @author: LOCRIAN_V
 * @date: 7/23/2026 1:37 下午
 */
@Configuration
public class TaskSchedulerConfig {

    /**
     * 用于模拟mysql主从同步延迟
     */
    @Bean
    TaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(4);
        threadPoolTaskScheduler.setThreadNamePrefix("cache-delay-delete-");
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

}
