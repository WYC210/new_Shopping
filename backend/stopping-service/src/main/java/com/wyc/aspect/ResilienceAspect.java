package com.wyc.aspect;

import com.wyc.exception.ServiceException;
import com.wyc.utils.R;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeoutException;

/**
 * 弹性服务切面，用于处理服务降级
 */
@Aspect
@Component
public class ResilienceAspect {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceAspect.class);

    /**
     * 环绕通知，处理被CircuitBreaker、RateLimiter或Bulkhead注解标记的方法
     * 当这些方法抛出异常时，提供统一的服务降级处理
     */
    @Around("@annotation(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker) || " +
            "@annotation(io.github.resilience4j.ratelimiter.annotation.RateLimiter) || " +
            "@annotation(io.github.resilience4j.bulkhead.annotation.Bulkhead)")
    public Object handleResilience(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            logger.error("服务调用失败: {}.{}, 异常: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    t.getMessage());
            return R.error(503, "服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 熔断器失败回调
     */
    public Object circuitBreakerFallback(Throwable throwable) {
        logger.error("熔断器触发，执行服务降级: {}", throwable.getMessage());
        if (throwable instanceof ServiceException) {
            ServiceException se = (ServiceException) throwable;
            Integer code = se.getCode();
            return code != null ? R.error(code, se.getMessage()) : R.error(400, se.getMessage());
        } else if (throwable instanceof TimeoutException) {
            return R.error(408, "服务调用超时，请稍后重试");
        }
        return R.error(500, "服务暂时不可用，请稍后重试");
    }

    /**
     * 限流器失败回调
     */
    public Object rateLimiterFallback(Throwable throwable) {
        logger.error("限流器触发，执行服务降级: {}", throwable.getMessage());
        if (throwable instanceof ServiceException) {
            ServiceException se = (ServiceException) throwable;
            Integer code = se.getCode();
            return code != null ? R.error(code, se.getMessage()) : R.error(400, se.getMessage());
        }
        return R.error(429, "服务繁忙，请稍后重试");
    }

    /**
     * 舱壁模式失败回调
     */
    public Object bulkheadFallback(Throwable throwable) {
        logger.error("并发控制触发，执行服务降级: {}", throwable.getMessage());
        if (throwable instanceof ServiceException) {
            ServiceException se = (ServiceException) throwable;
            Integer code = se.getCode();
            return code != null ? R.error(code, se.getMessage()) : R.error(400, se.getMessage());
        }
        return R.error(429, "服务繁忙，请稍后重试");
    }

    /**
     * 通用降级处理
     */
    private Object fallback(ProceedingJoinPoint joinPoint, Exception e) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        logger.error("{}类的{}方法发生异常，执行降级策略: {}", className, methodName, e.getMessage());

        // 业务异常直接返回给前端
        if (e instanceof ServiceException) {
            ServiceException se = (ServiceException) e;
            Integer code = se.getCode();
            return code != null ? R.error(code, se.getMessage()) : R.error(400, se.getMessage());
        }
        // 根据不同的异常类型返回不同的降级结果
        else if (e instanceof TimeoutException) {
            return R.error(408, "服务调用超时，请稍后重试");
        }

        return R.error(500, "服务暂时不可用，请稍后重试");
    }
}