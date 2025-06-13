package com.wyc.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j 配置类
 * 用于配置熔断器、限流器、舱壁模式等高并发保护机制
 */
@Configuration
public class Resilience4jConfig {

    /**
     * 熔断器配置
     */
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // 失败率阈值百分比，超过此值开启熔断
                .failureRateThreshold(10)
                // 慢调用阈值百分比，超过此值开启熔断
                .slowCallRateThreshold(10)
                // 慢调用的时间阈值，超过此时间的调用视为慢调用
                .slowCallDurationThreshold(Duration.ofSeconds(1))
                // 滑动窗口类型，基于计数或时间
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // 滑动窗口大小
                .slidingWindowSize(10)
                // 最小调用次数，只有达到此次数才会计算失败率
                .minimumNumberOfCalls(2)
                // 自动从开启状态转换为半开状态的等待时间
                .waitDurationInOpenState(Duration.ofSeconds(10))
                // 半开状态下允许的调用次数
                .permittedNumberOfCallsInHalfOpenState(3)
                // 自动从半开状态转换为关闭状态的等待时间
                .maxWaitDurationInHalfOpenState(Duration.ofSeconds(30))
                // 记录异常类型
                .recordExceptions(Exception.class)
                .build();
    }

    /**
     * 时间限制器配置
     */
    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom()
                // 超时时间
                .timeoutDuration(Duration.ofSeconds(3))
                // 是否可以取消执行
                .cancelRunningFuture(true)
                .build();
    }

    /**
     * 舱壁模式配置（并发控制）
     */
    @Bean
    public BulkheadConfig bulkheadConfig() {
        return BulkheadConfig.custom()
                // 最大并发调用数
                .maxConcurrentCalls(20)
                // 当达到最大并发调用数时，等待获取许可的最大时间
                .maxWaitDuration(Duration.ofMillis(500))
                .build();
    }

    /**
     * 限流器配置
     */
    @Bean
    public RateLimiterConfig rateLimiterConfig() {
        return RateLimiterConfig.custom()
                // 在刷新周期内允许的请求数
                .limitForPeriod(10)
                // 限流器刷新周期
                .limitRefreshPeriod(Duration.ofSeconds(1))
                // 等待获取许可的超时时间
                .timeoutDuration(Duration.ofMillis(500))
                .build();
    }

    /**
     * 熔断器注册表
     */
    @Bean
    public io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry(
            CircuitBreakerConfig config) {
        return io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.of(config);
    }

    /**
     * 限流器注册表
     */
    @Bean
    public io.github.resilience4j.ratelimiter.RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig config) {
        return io.github.resilience4j.ratelimiter.RateLimiterRegistry.of(config);
    }

    /**
     * 舱壁注册表
     */
    @Bean
    public io.github.resilience4j.bulkhead.BulkheadRegistry bulkheadRegistry(BulkheadConfig config) {
        return io.github.resilience4j.bulkhead.BulkheadRegistry.of(config);
    }
}