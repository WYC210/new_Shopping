package com.wyc.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * 搜索服务弹性配置
 * 处理搜索服务连接异常和降级
 *
 * @author wyc
 */
@Configuration
public class SearchServiceResilienceConfig {
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceResilienceConfig.class);

    /**
     * 搜索服务熔断器配置
     */
    @Bean("searchServiceCircuitBreakerConfigCustom")
    public CircuitBreakerConfig searchServiceCircuitBreakerConfigCustom() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 失败率阈值
                .waitDurationInOpenState(Duration.ofSeconds(10)) // 开启状态等待时间
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10) // 滑动窗口大小
                .minimumNumberOfCalls(5) // 最小调用次数
                .permittedNumberOfCallsInHalfOpenState(3) // 半开状态允许的调用次数
                .recordExceptions( // 需要记录的异常
                        ConnectException.class,
                        TimeoutException.class,
                        IOException.class,
                        Exception.class)
                .build();
    }

    /**
     * 搜索服务熔断器
     */
    @Bean("searchServiceCircuitBreaker")
    public CircuitBreaker searchServiceCircuitBreaker(
            @Qualifier("circuitBreakerRegistry") CircuitBreakerRegistry registry,
            @Qualifier("searchServiceCircuitBreakerConfigCustom") CircuitBreakerConfig searchServiceCircuitBreakerConfig) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("searchService", searchServiceCircuitBreakerConfig);

        // 添加状态转换监听器，便于记录日志和监控
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    logger.info("搜索服务熔断器状态变更: {} -> {}",
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState());
                })
                .onError(event -> {
                    logger.error("搜索服务调用失败，异常类型: {}, 错误消息: {}",
                            event.getThrowable().getClass().getName(),
                            event.getThrowable().getMessage());
                })
                .onSuccess(event -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("搜索服务调用成功，耗时: {} ms", event.getElapsedDuration().toMillis());
                    }
                });

        return circuitBreaker;
    }

    /**
     * 购物车熔断器配置
     */
    @Bean("cartCircuitBreakerConfig")
    public CircuitBreakerConfig cartCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // 失败率阈值百分比，超过此值开启熔断
                .failureRateThreshold(20)
                // 慢调用阈值百分比，超过此值开启熔断
                .slowCallRateThreshold(20)
                // 慢调用的时间阈值，超过此时间的调用视为慢调用
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                // 滑动窗口类型，基于计数或时间
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // 滑动窗口大小
                .slidingWindowSize(10)
                // 最小调用次数，只有达到此次数才会计算失败率
                .minimumNumberOfCalls(2)
                // 自动从开启状态转换为半开状态的等待时间
                .waitDurationInOpenState(Duration.ofSeconds(5))
                // 半开状态下允许的调用次数
                .permittedNumberOfCallsInHalfOpenState(2)
                // 自动从半开状态转换为关闭状态的等待时间
                .maxWaitDurationInHalfOpenState(Duration.ofSeconds(20))
                // 记录异常类型
                .recordExceptions(Exception.class)
                .build();
    }

    /**
     * 购物车熔断器
     */
    @Bean("cartCircuitBreaker")
    public CircuitBreaker cartCircuitBreaker(
            @Qualifier("circuitBreakerRegistry") CircuitBreakerRegistry registry,
            @Qualifier("cartCircuitBreakerConfig") CircuitBreakerConfig cartCircuitBreakerConfig) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("cartService", cartCircuitBreakerConfig);

        // 添加状态转换监听器
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    logger.info("购物车服务熔断器状态变更: {} -> {}",
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState());
                });

        return circuitBreaker;
    }

    /**
     * 购物车时间限制器配置
     */
    @Bean("cartTimeLimiterConfig")
    public TimeLimiterConfig cartTimeLimiterConfig() {
        return TimeLimiterConfig.custom()
                // 超时时间
                .timeoutDuration(Duration.ofSeconds(1))
                // 是否可以取消执行
                .cancelRunningFuture(true)
                .build();
    }
}