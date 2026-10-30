package com.isiniu.single.config;

import com.isiniu.common.web.MdcTaskDecorator;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 业务异步线程池。禁止使用 {@code Executors} 快捷工厂或 {@code new Thread}。
 *
 * @author isiniu
 */
@Configuration
public class AsyncExecutorConfig {

    @Bean(name = "bizExecutor")
    public ThreadPoolTaskExecutor bizExecutor(MdcTaskDecorator mdcTaskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("biz-");
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
