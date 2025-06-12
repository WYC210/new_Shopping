package com.wyc.listener;

import com.rabbitmq.client.Channel;
import com.wyc.config.RabbitMQConfig;
import com.wyc.domain.po.Orders;
import com.wyc.exception.ServiceException;
import com.wyc.service.IMessageProcessService;
import com.wyc.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * 订单消息监听器
 */
@Component
public class OrderMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderMessageListener.class);

    @Autowired
    private IOrderService orderService;

    @Autowired
    private IMessageProcessService messageProcessService;

    /**
     * 处理订单创建消息
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrderCreatedMessage(Map<String, Object> data, Message message, Channel channel)
            throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 获取消息ID，如果没有则生成一个
        String messageId = message.getMessageProperties().getMessageId();
        if (messageId == null) {
            messageId = UUID.randomUUID().toString();
        }

        try {
            logger.info("收到订单创建消息: {}", data);

            // 检查消息是否已处理
            if (messageProcessService.isMessageProcessed(messageId)) {
                logger.info("消息已处理，忽略: messageId={}", messageId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 记录消息处理开始
            messageProcessService.recordMessageStart(messageId, "ORDER_CREATED");

            Long orderId = Long.valueOf(data.get("orderId").toString());
            // 处理订单创建逻辑
            // ...

            // 记录消息处理成功
            messageProcessService.recordMessageSuccess(messageId);

            // 确认消息
            channel.basicAck(deliveryTag, false);
            logger.info("订单创建消息处理完成: orderId={}", orderId);
        } catch (Exception e) {
            logger.error("处理订单创建消息失败", e);

            // 记录消息处理失败
            messageProcessService.recordMessageFailure(messageId, e.getMessage());

            // 拒绝消息，并重新入队
            channel.basicNack(deliveryTag, false, true);
        }
    }

    /**
     * 处理订单取消消息（延迟队列的死信）
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCEL_QUEUE)
    public void handleOrderCancelMessage(Map<String, Object> data, Message message, Channel channel)
            throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        // 获取消息ID，如果没有则生成一个
        String messageId = message.getMessageProperties().getMessageId();
        if (messageId == null) {
            messageId = UUID.randomUUID().toString();
        }

        try {
            logger.info("收到订单取消消息: {}", data);

            // 临时解决方案：直接处理消息，不使用消息处理记录表
            // 等数据库表创建后再使用消息处理记录机制
            /*
             * // 检查消息是否已处理
             * if (messageProcessService.isMessageProcessed(messageId)) {
             * logger.info("消息已处理，忽略: messageId={}", messageId);
             * channel.basicAck(deliveryTag, false);
             * return;
             * }
             * 
             * // 记录消息处理开始
             * try {
             * messageProcessService.recordMessageStart(messageId, "ORDER_CANCEL");
             * } catch (Exception e) {
             * logger.warn("记录消息处理开始失败，但不影响处理流程: {}", e.getMessage());
             * }
             */

            Long orderId = Long.valueOf(data.get("orderId").toString());

            try {
                // 先检查订单状态，只有待支付状态的订单才能取消
                Orders order = orderService.getOrder(orderId);
                if (order == null) {
                    logger.warn("订单不存在，忽略取消消息: orderId={}", orderId);
                    channel.basicAck(deliveryTag, false);
                    return;
                }

                if (!"pending".equals(order.getStatus())) {
                    logger.info("订单状态不是待支付，忽略取消消息: orderId={}, status={}", orderId, order.getStatus());
                    channel.basicAck(deliveryTag, false);
                    return;
                }

                // 处理订单取消逻辑
                orderService.cancelOrder(orderId, null);

                // 确认消息
                channel.basicAck(deliveryTag, false);
                logger.info("订单取消消息处理完成: orderId={}", orderId);
            } catch (ServiceException e) {
                // 业务异常，记录错误但确认消息（不再重试）
                logger.warn("订单取消业务异常: orderId={}, error={}", orderId, e.getMessage());
                channel.basicAck(deliveryTag, false);
            }
        } catch (Exception e) {
            logger.error("处理订单取消消息失败", e);

            // 拒绝消息，根据异常情况决定是否重新入队
            // 如果是业务异常，不再重试；如果是系统异常，可以重试
            boolean requeue = !(e instanceof ServiceException);
            channel.basicNack(deliveryTag, false, requeue);
        }
    }
}