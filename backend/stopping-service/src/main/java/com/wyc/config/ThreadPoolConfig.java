package com.wyc.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);

    @Value("${thread-pool.core-pool-size}")
    private int corePoolSize;

    @Value("${thread-pool.max-pool-size}")
    private int maxPoolSize;

    @Value("${thread-pool.queue-capacity}")
    private int queueCapacity;

    @Value("${thread-pool.keep-alive-seconds}")
    private int keepAliveSeconds;

    @Value("${thread-pool.thread-name-prefix}")
    private String threadNamePrefix;

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        logger.info(
                "初始化线程池: corePoolSize={}, maxPoolSize={}, queueCapacity={}, keepAliveSeconds={}, threadNamePrefix={}",
                corePoolSize, maxPoolSize, queueCapacity, keepAliveSeconds, threadNamePrefix);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 队列大小
        executor.setQueueCapacity(queueCapacity);
        // 线程前缀名
        executor.setThreadNamePrefix(threadNamePrefix);
        // 线程存活时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 初始化
        executor.initialize();

        // 使用TTL包装，支持线程间上下文传递
        return TtlExecutors.getTtlExecutor(executor);
    }

    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        logger.info("初始化异步线程池");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(corePoolSize / 2);
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize / 2);
        // 队列大小
        executor.setQueueCapacity(queueCapacity / 2);
        // 线程前缀名
        executor.setThreadNamePrefix(threadNamePrefix + "event-");
        // 线程存活时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 初始化
        executor.initialize();

        // 使用TTL包装，支持线程间上下文传递
        return TtlExecutors.getTtlExecutor(executor);
    }
}