package com.wyc.controller;

import com.wyc.service.impl.ResilientServiceImpl;
import com.wyc.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 弹性服务控制器
 * 用于测试弹性服务功能
 */
@RestController
@RequestMapping("/api/resilient")
public class ResilientController {

    @Autowired
    private ResilientServiceImpl resilientService;

    /**
     * 测试熔断器
     */
    @GetMapping("/circuit-breaker/{orderId}")
    public R<?> testCircuitBreaker(@PathVariable Long orderId) {
        return resilientService.getOrderWithCircuitBreaker(orderId);
    }

    /**
     * 测试限流器
     */
    @GetMapping("/rate-limiter/{orderId}")
    public R<?> testRateLimiter(@PathVariable Long orderId) {
        return resilientService.createOrderWithRateLimiter(orderId);
    }

    /**
     * 测试舱壁模式
     */
    @GetMapping("/bulkhead/{orderId}")
    public R<?> testBulkhead(@PathVariable Long orderId) {
        return resilientService.processOrderWithBulkhead(orderId);
    }

    /**
     * 测试超时控制
     */
    @GetMapping("/timeout/{orderId}")
    public R<?> testTimeout(@PathVariable Long orderId) throws ExecutionException, InterruptedException {
        CompletableFuture<R<?>> future = resilientService.getOrderWithTimeout(orderId);
        return future.get(); // 阻塞等待结果
    }

    /**
     * 测试完整保护
     */
    @GetMapping("/full-protection/{orderId}")
    public R<?> testFullProtection(@PathVariable Long orderId) {
        return resilientService.getOrderWithFullProtection(orderId);
    }

    /**
     * 测试自定义注解
     */
    @GetMapping("/resilience/{orderId}")
    public R<?> testResilience(@PathVariable Long orderId) {
        return resilientService.getOrderWithResilience(orderId);
    }

    /**
     * 测试异步执行
     */
    @GetMapping("/async/{orderId}")
    public R<?> testAsync(@PathVariable Long orderId) throws ExecutionException, InterruptedException {
        CompletableFuture<R<?>> future = resilientService.getOrderAsync(orderId);
        return future.get(); // 阻塞等待结果
    }
}