package com.wyc.exception;

/**
 * 弹性服务异常
 * 用于表示服务限流、熔断等异常情况
 */
public class ResilienceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private String type;

    public ResilienceException() {
        super();
    }

    public ResilienceException(String message) {
        super(message);
        this.message = message;
    }

    public ResilienceException(String message, Integer code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public ResilienceException(String message, String type) {
        super(message);
        this.message = message;
        this.type = type;
    }

    public ResilienceException(String message, Integer code, String type) {
        super(message);
        this.message = message;
        this.code = code;
        this.type = type;
    }

    public static ResilienceException circuitBreaker(String message) {
        return new ResilienceException(message, 503, "CircuitBreaker");
    }

    public static ResilienceException rateLimiter(String message) {
        return new ResilienceException(message, 429, "RateLimiter");
    }

    public static ResilienceException bulkhead(String message) {
        return new ResilienceException(message, 429, "Bulkhead");
    }

    public static ResilienceException timeout(String message) {
        return new ResilienceException(message, 408, "Timeout");
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public String getType() {
        return type;
    }
}