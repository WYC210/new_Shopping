package com.wyc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 弹性服务注解
 * 用于标记需要进行服务降级、熔断、限流等高并发保护的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Resilience {
    /**
     * 熔断器名称
     */
    String circuitBreaker() default "";
    
    /**
     * 限流器名称
     */
    String rateLimiter() default "";
    
    /**
     * 舱壁名称
     */
    String bulkhead() default "";
    
    /**
     * 超时时间（毫秒）
     */
    long timeout() default 3000;
    
    /**
     * 降级方法名称
     */
    String fallbackMethod() default "";
} 