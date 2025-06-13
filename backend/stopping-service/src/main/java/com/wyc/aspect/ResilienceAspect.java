package com.wyc.aspect;
import com.wyc.utils.R;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeoutException;

/**
 * 弹性服务切面
 * 用于处理服务降级、熔断、限流等高并发场景
 */
@Aspect
@Component
public class ResilienceAspect {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceAspect.class);

    /**
     * 环绕通知，处理带有@Resilience注解的方法
     */
    @Around("@annotation(com.wyc.annotation.Resilience)")
    public Object handleResilience(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            logger.error("服务调用异常，执行降级策略: {}", e.getMessage(), e);
            return fallback(joinPoint, e);
        }
    }

    /**
     * 熔断器失败回调
     */
    public Object circuitBreakerFallback(Throwable throwable) {
        logger.error("熔断器触发，执行服务降级: {}", throwable.getMessage());
        if (throwable instanceof TimeoutException) {
            return R.error(408, "服务调用超时，请稍后重试");
        }
        return R.error(500, "服务暂时不可用，请稍后重试");
    }

    /**
     * 限流器失败回调
     */
    public Object rateLimiterFallback(Throwable throwable) {
        logger.error("限流器触发，执行服务降级: {}", throwable.getMessage());
        return R.error(429, "服务繁忙，请稍后重试");
    }

    /**
     * 舱壁模式失败回调
     */
    public Object bulkheadFallback(Throwable throwable) {
        logger.error("并发控制触发，执行服务降级: {}", throwable.getMessage());
        return R.error(429, "服务繁忙，请稍后重试");
    }

    /**
     * 通用降级处理
     */
    private Object fallback(ProceedingJoinPoint joinPoint, Exception e) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        logger.error("{}类的{}方法发生异常，执行降级策略: {}", className, methodName, e.getMessage());

        // 根据不同的异常类型返回不同的降级结果
        if (e instanceof TimeoutException) {
            return R.error(408, "服务调用超时，请稍后重试");
        }

        return R.error(500, "服务暂时不可用，请稍后重试");
    }
}