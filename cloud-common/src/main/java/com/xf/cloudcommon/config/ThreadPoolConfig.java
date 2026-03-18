package com.xf.cloudcommon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 * 核心：注入 MdcTaskDecorator 实现分布式链路追踪 ID 在多线程间的透传
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 核心线程数 (根据 CPU 核数动态调整)
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        // 2. 最大线程数
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        // 3. 队列容量
        executor.setQueueCapacity(500);
        // 4. 线程活跃时间
        executor.setKeepAliveSeconds(60);
        // 5. 线程名称前缀，方便日志定位到这是业务线程池
        executor.setThreadNamePrefix("async-xf-");

        // --- 核心重点：配置装饰器，实现 MDC 透传 ---
        executor.setTaskDecorator(new MdcTaskDecorator());

        // 6. 拒绝策略：由主线程执行，防止任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池 (Graceful Shutdown)
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}
