package com.wyc.service.impl;

import com.wyc.annotation.Resilience;
import com.wyc.exception.ResilienceException;
import com.wyc.exception.ServiceException;
import com.wyc.utils.R;
import com.wyc.utils.ResilienceUtil;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * 弹性服务示例实现类
 * 展示如何使用熔断和服务降级功能
 */
@Service
public class ResilientServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(ResilientServiceImpl.class);

    @Autowired
    private ResilienceUtil resilienceUtil;

    /**
     * 使用注解方式的熔断器保护
     */
    @CircuitBreaker(name = "orderService", fallbackMethod = "orderServiceFallback")
    public R<?> getOrderWithCircuitBreaker(Long orderId) {
        logger.info("获取订单信息: orderId={}", orderId);

        // 模拟随机失败，用于测试熔断器
        if (Math.random() > 0.7) {
            throw new RuntimeException("服务暂时不可用");
        }

        // 正常业务逻辑
        return R.ok("获取订单成功", "订单数据: " + orderId);
    }

    /**
     * 订单服务熔断器降级方法
     */
    public R<?> orderServiceFallback(Long orderId, Throwable e) {
        logger.error("订单服务熔断，执行降级策略: orderId={}, error={}", orderId, e.getMessage());

        // 如果是业务异常，直接返回业务异常信息
        if (e instanceof ServiceException) {
            ServiceException se = (ServiceException) e;
            Integer code = se.getCode();
            return code != null ? R.error(code, se.getMessage()) : R.error(400, se.getMessage());
        }

        return R.error(503, "订单服务暂时不可用，请稍后重试");
    }

    /**
     * 使用注解方式的限流器保护
     */
    @RateLimiter(name = "orderService", fallbackMethod = "rateLimiterFallback")
    public R<?> createOrderWithRateLimiter(Object orderData) {
        logger.info("创建订单: {}", orderData);

        // 正常业务逻辑
        return R.ok("创建订单成功");
    }

    /**
     * 限流器降级方法
     */
    public R<?> rateLimiterFallback(Object orderData, Throwable e) {
        logger.error("订单创建限流，执行降级策略: error={}", e.getMessage());

        // 如果是业务异常，直接返回业务异常信息
        if (e instanceof ServiceException) {
            ServiceException se = (ServiceException) e;
            Integer code = se.getCode();
            return code != null ? R.error(code, se.getMessage()) : R.error(400, se.getMessage());
        }

        return R.error(429, "请求过于频繁，请稍后重试");
    }

    /**
     * 使用注解方式的舱壁模式保护
     */
    @Bulkhead(name = "orderService", fallbackMethod = "bulkheadFallback")
    public R<?> processOrderWithBulkhead(Long orderId) {
        logger.info("处理订单: orderId={}", orderId);

        // 模拟耗时操作
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 正常业务逻辑
        return R.ok("处理订单成功");
    }

    /**
     * 舱壁模式降级方法
     */
    public R<?> bulkheadFallback(Long orderId, Throwable e) {
        logger.error("订单处理并发控制，执行降级策略: orderId={}, error={}", orderId, e.getMessage());
        return R.error(429, "服务繁忙，请稍后重试");
    }

    /**
     * 使用注解方式的超时控制
     */
    @TimeLimiter(name = "orderService", fallbackMethod = "timeoutFallback")
    public CompletableFuture<R<?>> getOrderWithTimeout(Long orderId) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("获取订单信息(带超时控制): orderId={}", orderId);

            // 模拟随机超时
            try {
                if (Math.random() > 0.7) {
                    Thread.sleep(5000); // 超时
                } else {
                    Thread.sleep(500); // 正常
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 正常业务逻辑
            return R.ok("获取订单成功", "订单数据: " + orderId);
        });
    }

    /**
     * 超时控制降级方法
     */
    public CompletableFuture<R<?>> timeoutFallback(Long orderId, Throwable e) {
        logger.error("订单查询超时，执行降级策略: orderId={}, error={}", orderId, e.getMessage());
        return CompletableFuture.completedFuture(R.error(408, "服务调用超时，请稍后重试"));
    }

    /**
     * 使用工具类方式的完整保护
     */
    public R<?> getOrderWithFullProtection(Long orderId) {
        logger.info("获取订单信息(完整保护): orderId={}", orderId);

        try {
            // 使用工具类执行受保护的方法
            return resilienceUtil.executeWithFullProtection(() -> {
                // 模拟随机失败
                if (Math.random() > 0.7) {
                    throw new RuntimeException("服务暂时不可用");
                }

                // 正常业务逻辑
                return R.ok("获取订单成功", "订单数据: " + orderId);
            }, "orderService");
        } catch (Exception e) {
            logger.error("订单服务异常，执行降级策略: orderId={}, error={}", orderId, e.getMessage());
            return R.error(503, "订单服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 使用自定义注解方式的服务保护
     */
    @Resilience
    public R<?> getOrderWithResilience(Long orderId) {
        logger.info("获取订单信息(自定义注解): orderId={}", orderId);

        // 模拟随机失败
        if (Math.random() > 0.7) {
            throw new ResilienceException("服务暂时不可用", 503, "CircuitBreaker");
        }

        // 正常业务逻辑
        return R.ok("获取订单成功", "订单数据: " + orderId);
    }

    /**
     * 异步执行带保护的方法
     */
    public CompletableFuture<R<?>> getOrderAsync(Long orderId) {
        logger.info("异步获取订单信息: orderId={}", orderId);

        // 使用工具类异步执行受保护的方法
        Supplier<R<?>> supplier = () -> {
            // 模拟随机失败
            if (Math.random() > 0.7) {
                throw new RuntimeException("服务暂时不可用");
            }
            // 模拟处理延迟
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 正常业务逻辑
            return R.ok("获取订单成功", "订单数据: " + orderId);
        };
        return resilienceUtil.executeAsync(supplier, "orderService")
                .thenApply(r -> (R<?>) r)
                .<R<?>>thenApply(r -> r)
                .exceptionally(e -> {
                    logger.error("订单服务异步调用异常，执行降级策略: orderId={}, error={}", orderId, e.getMessage());
                    return R.error(503, "订单服务暂时不可用，请稍后重试");
                });
    }
}