# 服务降级和熔断处理

本项目使用 Resilience4j 实现了服务降级和熔断处理功能，用于应对高并发场景下的系统稳定性问题。

## 功能特性

1. **熔断器(Circuit Breaker)**

   - 当服务调用失败率达到阈值时自动开启熔断
   - 防止级联故障，保护系统稳定性
   - 支持半开状态自动恢复机制

2. **限流器(Rate Limiter)**

   - 控制 API 的访问频率
   - 防止过载和资源耗尽
   - 支持基于时间窗口的限流策略

3. **舱壁模式(Bulkhead)**

   - 限制并发调用数量
   - 隔离不同服务的资源使用
   - 防止单个服务故障影响整个系统

4. **超时控制(Timeout)**

   - 为服务调用设置最大响应时间
   - 避免长时间等待导致资源耗尽
   - 支持自动取消超时的执行

5. **服务降级(Fallback)**
   - 当服务不可用时提供备选方案
   - 返回缓存数据或默认响应
   - 确保用户体验不会严重受损

## 使用方式

### 1. 注解方式

使用 Resilience4j 提供的注解可以轻松实现服务保护：

```java
// 熔断器
@CircuitBreaker(name = "orderService", fallbackMethod = "orderServiceFallback")
public R<?> getOrder(Long orderId) {
    // 业务逻辑
}

// 降级方法
public R<?> orderServiceFallback(Long orderId, Exception e) {
    return R.error("服务暂时不可用，请稍后重试");
}

// 限流器
@RateLimiter(name = "orderService", fallbackMethod = "rateLimiterFallback")
public R<?> createOrder(Object orderData) {
    // 业务逻辑
}

// 舱壁模式
@Bulkhead(name = "orderService", fallbackMethod = "bulkheadFallback")
public R<?> processOrder(Long orderId) {
    // 业务逻辑
}

// 超时控制
@TimeLimiter(name = "orderService", fallbackMethod = "timeoutFallback")
public CompletableFuture<R<?>> getOrderWithTimeout(Long orderId) {
    return CompletableFuture.supplyAsync(() -> {
        // 业务逻辑
    });
}
```

### 2. 编程方式

使用`ResilienceUtil`工具类可以更灵活地实现服务保护：

```java
// 完整保护（熔断器 + 限流器 + 舱壁模式）
public R<?> getOrder(Long orderId) {
    try {
        return resilienceUtil.executeWithFullProtection(() -> {
            // 业务逻辑
            return R.ok("获取订单成功", orderData);
        }, "orderService");
    } catch (Exception e) {
        return R.error("服务暂时不可用，请稍后重试");
    }
}

// 异步执行
public CompletableFuture<R<?>> getOrderAsync(Long orderId) {
    return resilienceUtil.executeAsync(() -> {
        // 业务逻辑
        return R.ok("获取订单成功", orderData);
    }, "orderService").exceptionally(e -> {
        return R.error("服务暂时不可用，请稍后重试");
    });
}
```

### 3. 自定义注解

使用自定义的`@Resilience`注解可以统一管理服务保护策略：

```java
@Resilience
public R<?> getOrder(Long orderId) {
    // 业务逻辑
}
```

## 配置说明

在`application.yml`中配置 Resilience4j 的参数：

```yaml
resilience4j:
  circuitbreaker:
    instances:
      orderService:
        failureRateThreshold: 50
        waitDurationInOpenState: 5s
        # 更多配置...
  ratelimiter:
    instances:
      orderService:
        limitForPeriod: 50
        limitRefreshPeriod: 1s
        # 更多配置...
  bulkhead:
    instances:
      orderService:
        maxConcurrentCalls: 20
        # 更多配置...
  timelimiter:
    instances:
      orderService:
        timeoutDuration: 3s
        # 更多配置...
```

## 监控与管理

通过 Spring Boot Actuator 可以监控 Resilience4j 的运行状态：

- 熔断器状态: `/actuator/circuitbreakers`
- 限流器状态: `/actuator/ratelimiters`
- 舱壁状态: `/actuator/bulkheads`
- 健康状态: `/actuator/health`

## 最佳实践

1. 为不同的服务设置不同的保护策略
2. 根据服务特性调整参数（如超时时间、并发数）
3. 提供合理的降级策略，确保用户体验
4. 监控系统运行状态，及时调整配置参数
5. 结合缓存策略，减轻系统负载
