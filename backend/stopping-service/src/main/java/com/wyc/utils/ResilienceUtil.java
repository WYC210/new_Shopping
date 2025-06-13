package com.wyc.utils;

import com.wyc.exception.ResilienceException;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 弹性服务工具类
 * 用于方便地创建和使用熔断器、限流器和舱壁模式
 */
@Component
public class ResilienceUtil {



    @Autowired
    private io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry;
    @Autowired
    private io.github.resilience4j.ratelimiter.RateLimiterRegistry rateLimiterRegistry;
    @Autowired
    private io.github.resilience4j.bulkhead.BulkheadRegistry bulkheadRegistry;

    /**
     * 执行带熔断器保护的方法
     *
     * @param supplier 方法
     * @param name     熔断器名称
     * @param <T>      返回类型
     * @return 方法执行结果
     */
    public <T> T executeWithCircuitBreaker(Supplier<T> supplier, String name) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name);
        return CircuitBreaker.decorateSupplier(circuitBreaker, supplier).get();
    }

    /**
     * 执行带限流器保护的方法
     *
     * @param supplier 方法
     * @param name     限流器名称
     * @param <T>      返回类型
     * @return 方法执行结果
     */
    public <T> T executeWithRateLimiter(Supplier<T> supplier, String name) {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(name);
        return RateLimiter.decorateSupplier(rateLimiter, supplier).get();
    }

    /**
     * 执行带舱壁模式保护的方法
     *
     * @param supplier 方法
     * @param name     舱壁名称
     * @param <T>      返回类型
     * @return 方法执行结果
     */
    public <T> T executeWithBulkhead(Supplier<T> supplier, String name) {
        Bulkhead bulkhead = bulkheadRegistry.bulkhead(name);
        return Bulkhead.decorateSupplier(bulkhead, supplier).get();
    }

    /**
     * 执行带完整保护的方法（熔断器 + 限流器 + 舱壁模式）
     *
     * @param supplier 方法
     * @param name     名称前缀
     * @param <T>      返回类型
     * @return 方法执行结果
     */
    public <T> T executeWithFullProtection(Supplier<T> supplier, String name) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name + "-cb");
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(name + "-rl");
        Bulkhead bulkhead = bulkheadRegistry.bulkhead(name + "-bh");

        // 组合多个装饰器
        Supplier<T> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker,
                RateLimiter.decorateSupplier(rateLimiter,
                        Bulkhead.decorateSupplier(bulkhead, supplier)));

        return decoratedSupplier.get();
    }

    /**
     * 异步执行带完整保护的方法
     *
     * @param supplier 方法
     * @param name     名称前缀
     * @param <T>      返回类型
     * @return CompletableFuture
     */
    public <T> CompletableFuture<T> executeAsync(Supplier<T> supplier, String name) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(name + "-cb");
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(name + "-rl");
        Bulkhead bulkhead = bulkheadRegistry.bulkhead(name + "-bh");

        // 组合多个装饰器
        Supplier<T> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker,
                RateLimiter.decorateSupplier(rateLimiter,
                        Bulkhead.decorateSupplier(bulkhead, supplier)));

        return CompletableFuture.supplyAsync(decoratedSupplier);
    }

    /**
     * 执行带超时控制的方法
     *
     * @param callable 方法
     * @param timeout  超时时间（毫秒）
     * @param <T>      返回类型
     * @return 方法执行结果
     * @throws Exception 执行异常
     */
    public <T> T executeWithTimeout(Callable<T> callable, long timeout) throws Exception {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            throw new ResilienceException("执行超时", 408, "Timeout");
        }
    }
}