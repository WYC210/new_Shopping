package com.wyc.handler;

import com.wyc.exception.ServiceException;
import com.wyc.utils.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

/**
 * 服务降级处理器
 * 用于处理各种服务异常情况，提供默认的降级策略
 */
@Component
public class FallbackHandler {

    private static final Logger logger = LoggerFactory.getLogger(FallbackHandler.class);

    /**
     * 通用服务降级处理
     *
     * @param e           异常
     * @param serviceName 服务名称
     * @return 降级响应
     */
    public R<?> handleFallback(Throwable e, String serviceName) {
        logger.error("服务[{}]调用失败，执行降级策略: {}", serviceName, e.getMessage(), e);

        // 如果是业务异常，直接返回业务异常信息
        if (e instanceof ServiceException) {
            ServiceException se = (ServiceException) e;
            Integer code = se.getCode();
            return code != null ? R.error(code, se.getMessage()) : R.error(400, se.getMessage());
        } else if (e instanceof TimeoutException) {
            return R.error(408, "服务调用超时，请稍后重试");
        } else if (e instanceof IllegalStateException && e.getMessage().contains("CircuitBreaker")) {
            return R.error(503, "服务暂时不可用，请稍后重试");
        } else if (e instanceof IllegalStateException && e.getMessage().contains("RateLimiter")) {
            return R.error(429, "请求过于频繁，请稍后重试");
        } else if (e instanceof IllegalStateException && e.getMessage().contains("Bulkhead")) {
            return R.error(429, "服务繁忙，请稍后重试");
        }

        return R.error(500, "系统繁忙，请稍后重试");
    }

    /**
     * 订单服务降级处理
     */
    public R<?> orderServiceFallback(Throwable e) {
        logger.error("订单服务调用失败，执行降级策略: {}", e.getMessage(), e);

        // 如果是业务异常，直接返回业务异常信息
        if (e instanceof ServiceException) {
            ServiceException se = (ServiceException) e;
            Integer code = se.getCode();
            return code != null ? R.error(code, se.getMessage()) : R.error(400, se.getMessage());
        }

        return R.error(503, "订单服务暂时不可用，请稍后重试");
    }

    /**
     * 支付服务降级处理
     */
    public R<?> paymentServiceFallback(Throwable e) {
        logger.error("支付服务调用失败，执行降级策略: {}", e.getMessage(), e);
        return R.error(503, "支付服务暂时不可用，请稍后重试");
    }

    /**
     * 用户服务降级处理
     */
    public R<?> userServiceFallback(Throwable e) {
        logger.error("用户服务调用失败，执行降级策略: {}", e.getMessage(), e);
        return R.error(503, "用户服务暂时不可用，请稍后重试");
    }

    /**
     * 商品服务降级处理
     */
    public R<?> productServiceFallback(Throwable e) {
        logger.error("商品服务调用失败，执行降级策略: {}", e.getMessage(), e);
        return R.error(503, "商品服务暂时不可用，请稍后重试");
    }
}