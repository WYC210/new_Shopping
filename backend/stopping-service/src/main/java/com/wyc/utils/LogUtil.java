package com.wyc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类，提供统一的日志记录方法
 */
public class LogUtil {

    /**
     * 获取指定类的日志记录器
     * 
     * @param clazz 类
     * @return 日志记录器
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 记录方法开始执行的日志
     * 
     * @param logger     日志记录器
     * @param methodName 方法名
     * @param args       方法参数
     */
    public static void logMethodStart(Logger logger, String methodName, Object... args) {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("开始执行: ").append(methodName).append("(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(args[i]);
            }
            sb.append(")");
            logger.info(sb.toString());
        }
    }

    /**
     * 记录方法执行成功的日志
     * 
     * @param logger     日志记录器
     * @param methodName 方法名
     * @param result     执行结果
     */
    public static void logMethodSuccess(Logger logger, String methodName, Object result) {
        if (logger.isInfoEnabled()) {
            logger.info("执行成功: {} -> {}", methodName, result);
        }
    }

    /**
     * 记录方法执行成功的日志（多参数版本）
     * 
     * @param logger     日志记录器
     * @param methodName 方法名
     * @param results    执行结果（多个）
     */
    public static void logMethodSuccess(Logger logger, String methodName, Object... results) {
        if (logger.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("执行成功: ").append(methodName).append(" -> ");
            for (int i = 0; i < results.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(results[i]);
            }
            logger.info(sb.toString());
        }
    }

    /**
     * 记录方法执行失败的日志
     * 
     * @param logger     日志记录器
     * @param methodName 方法名
     * @param e          异常
     */
    public static void logMethodError(Logger logger, String methodName, Throwable e) {
        logger.error("执行失败: {}, 异常: {}", methodName, e.getMessage(), e);
    }
}