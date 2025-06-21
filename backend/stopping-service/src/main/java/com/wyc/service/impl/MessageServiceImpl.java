package com.wyc.service.impl;

import com.wyc.config.RabbitMQConfig;
import com.wyc.service.IMessageService;
import com.wyc.utils.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 消息服务实现类
 */
@Service
public class MessageServiceImpl implements IMessageService {

    private static final Logger logger = LogUtil.getLogger(MessageServiceImpl.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Async("asyncExecutor")
    public void sendOrderCreatedMessage(Long orderId, Map<String, Object> data) {
        LogUtil.logMethodStart(logger, "sendOrderCreatedMessage", orderId, data);

        logger.info("发送订单创建消息: orderId={}", orderId);

        if (data == null) {
            data = new HashMap<>();
        }
        data.put("orderId", orderId);
        data.put("timestamp", System.currentTimeMillis());

        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_ROUTING_KEY,
                data,
                correlationData);

        logger.info("订单创建消息已发送: orderId={}, correlationId={}", orderId, correlationData.getId());

        LogUtil.logMethodSuccess(logger, "sendOrderCreatedMessage", orderId);
    }

    @Override
    @Async("asyncExecutor")
    public void sendPaymentSuccessMessage(Long orderId, Long paymentId, Map<String, Object> data) {
        LogUtil.logMethodStart(logger, "sendPaymentSuccessMessage", orderId, paymentId, data);

        logger.info("发送支付成功消息: orderId={}, paymentId={}", orderId, paymentId);

        if (data == null) {
            data = new HashMap<>();
        }
        data.put("orderId", orderId);
        data.put("paymentId", paymentId);
        data.put("timestamp", System.currentTimeMillis());

        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_ROUTING_KEY,
                data,
                correlationData);

        logger.info("支付成功消息已发送: orderId={}, paymentId={}, correlationId={}",
                orderId, paymentId, correlationData.getId());

        LogUtil.logMethodSuccess(logger, "sendPaymentSuccessMessage", orderId, paymentId);
    }

    @Override
    @Async("asyncExecutor")
    public void sendNotificationMessage(Long userId, String title, String content, Map<String, Object> data) {
        LogUtil.logMethodStart(logger, "sendNotificationMessage", userId, title, content, data);

        logger.info("发送通知消息: userId={}, title={}", userId, title);

        if (data == null) {
            data = new HashMap<>();
        }
        data.put("userId", userId);
        data.put("title", title);
        data.put("content", content);
        data.put("timestamp", System.currentTimeMillis());

        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                "", // Fanout交换机不需要路由键
                data,
                correlationData);

        logger.info("通知消息已发送: userId={}, title={}, correlationId={}",
                userId, title, correlationData.getId());

        LogUtil.logMethodSuccess(logger, "sendNotificationMessage", userId, title);
    }

    @Override
    @Async("asyncExecutor")
    public void sendDelayedOrderMessage(Long orderId, long delayMillis) {
        LogUtil.logMethodStart(logger, "sendDelayedOrderMessage", orderId, delayMillis);

        logger.info("发送延迟订单消息: orderId={}, delayMillis={}", orderId, delayMillis);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("timestamp", System.currentTimeMillis());
        data.put("expireTime", System.currentTimeMillis() + delayMillis);

        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        // 使用延迟队列
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.DELAYED_ORDER_ROUTING_KEY,
                data,
                message -> {
                    MessageProperties props = message.getMessageProperties();
                    props.setExpiration(String.valueOf(delayMillis));
                    return message;
                },
                correlationData);

        logger.info("延迟订单消息已发送: orderId={}, delayMillis={}, correlationId={}",
                orderId, delayMillis, correlationData.getId());

        LogUtil.logMethodSuccess(logger, "sendDelayedOrderMessage", orderId);
    }

    @Override
    @Async
    public void sendMessage(String topic, Map<String, Object> data) {
        LogUtil.logMethodStart(logger, "sendMessage", topic, data);

        // 这里可以集成消息队列如RabbitMQ、Kafka等
        // 开发阶段简单打印日志即可
        logger.info("发送消息: topic={}, data={}", topic, data);

        LogUtil.logMethodSuccess(logger, "sendMessage", topic);
    }

    @Override
    public void sendDelayedMessage(String topic, Map<String, Object> data, int delaySeconds) {
        LogUtil.logMethodStart(logger, "sendDelayedMessage", topic, data, delaySeconds);

        // 使用调度器实现延迟消息
        scheduler.schedule(() -> {
            logger.info("发送延迟消息: topic={}, data={}, delay={}s", topic, data, delaySeconds);
        }, delaySeconds, TimeUnit.SECONDS);

        LogUtil.logMethodSuccess(logger, "sendDelayedMessage", topic);
    }
}