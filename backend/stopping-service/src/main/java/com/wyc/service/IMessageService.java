package com.wyc.service;

import java.util.Map;

/**
 * 消息服务接口
 */
public interface IMessageService {

    /**
     * 发送消息
     *
     * @param topic 消息主题
     * @param data  消息数据
     */
    void sendMessage(String topic, Map<String, Object> data);

    /**
     * 发送延迟消息
     *
     * @param topic        消息主题
     * @param data         消息数据
     * @param delaySeconds 延迟秒数
     */
    void sendDelayedMessage(String topic, Map<String, Object> data, int delaySeconds);

    /**
     * 发送订单创建消息
     *
     * @param orderId 订单ID
     * @param data    订单数据
     */
    void sendOrderCreatedMessage(Long orderId, Map<String, Object> data);

    /**
     * 发送支付成功消息
     *
     * @param orderId   订单ID
     * @param paymentId 支付ID
     * @param data      支付数据
     */
    void sendPaymentSuccessMessage(Long orderId, Long paymentId, Map<String, Object> data);

    /**
     * 发送通知消息
     *
     * @param userId  用户ID
     * @param title   通知标题
     * @param content 通知内容
     * @param data    通知数据
     */
    void sendNotificationMessage(Long userId, String title, String content, Map<String, Object> data);

    /**
     * 发送延迟订单消息（用于订单超时取消）
     *
     * @param orderId     订单ID
     * @param delayMillis 延迟时间（毫秒）
     */
    void sendDelayedOrderMessage(Long orderId, long delayMillis);
}