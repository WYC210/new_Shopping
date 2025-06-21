package com.wyc.listener;

import com.wyc.service.IMessageService;
import com.wyc.service.event.OrderEvent;
import com.wyc.utils.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单事件监听器，处理订单相关事件
 */
@Component
public class OrderEventListener {

    private static final Logger logger = LogUtil.getLogger(OrderEventListener.class);

    @Autowired
    private IMessageService messageService;

    /**
     * 处理订单创建事件
     * 
     * @param event 订单事件
     */
    @EventListener
    public void handleOrderCreatedEvent(OrderEvent event) {
        if (event.getEventType() != OrderEvent.OrderEventType.CREATED) {
            return;
        }

        logger.info("接收到订单创建事件: orderId={}", event.getOrder().getOrderId());

        try {
            // 构建消息数据
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("orderId", event.getOrder().getOrderId());
            messageData.put("userId", event.getOrder().getUserId());
            messageData.put("status", event.getOrder().getStatus());
            messageData.put("amount", event.getOrder().getTotalAmount());
            messageData.put("time", event.getTimestamp());

            // 发送消息通知
            messageService.sendMessage("order.created", messageData);

            // 发送延迟消息用于订单超时取消
            messageService.sendDelayedMessage("order.timeout",
                    Map.of("orderId", event.getOrder().getOrderId()),
                    30 * 60); // 30分钟后超时

        } catch (Exception e) {
            logger.error("处理订单创建事件失败: orderId={}, error={}",
                    event.getOrder().getOrderId(), e.getMessage());
        }
    }

    /**
     * 处理订单支付成功事件
     * 
     * @param event 订单事件
     */
    @EventListener
    public void handleOrderPaidEvent(OrderEvent event) {
        if (event.getEventType() != OrderEvent.OrderEventType.PAID) {
            return;
        }

        logger.info("接收到订单支付成功事件: orderId={}", event.getOrder().getOrderId());

        try {
            // 构建消息数据
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("orderId", event.getOrder().getOrderId());
            messageData.put("userId", event.getOrder().getUserId());
            messageData.put("status", event.getOrder().getStatus());
            messageData.put("amount", event.getOrder().getTotalAmount());
            messageData.put("time", event.getTimestamp());
            messageData.put("paymentData", event.getData());

            // 发送消息通知
            messageService.sendMessage("order.payment.success", messageData);

        } catch (Exception e) {
            logger.error("处理订单支付成功事件失败: orderId={}, error={}",
                    event.getOrder().getOrderId(), e.getMessage());
        }
    }

    /**
     * 处理订单取消事件
     * 
     * @param event 订单事件
     */
    @EventListener
    public void handleOrderCancelledEvent(OrderEvent event) {
        if (event.getEventType() != OrderEvent.OrderEventType.CANCELLED) {
            return;
        }

        logger.info("接收到订单取消事件: orderId={}", event.getOrder().getOrderId());

        try {
            // 构建消息数据
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("orderId", event.getOrder().getOrderId());
            messageData.put("userId", event.getOrder().getUserId());
            messageData.put("status", event.getOrder().getStatus());
            messageData.put("time", event.getTimestamp());

            // 发送消息通知
            messageService.sendMessage("order.cancel", messageData);

        } catch (Exception e) {
            logger.error("处理订单取消事件失败: orderId={}, error={}",
                    event.getOrder().getOrderId(), e.getMessage());
        }
    }

    /**
     * 处理订单完成事件
     * 
     * @param event 订单事件
     */
    @EventListener
    public void handleOrderCompletedEvent(OrderEvent event) {
        if (event.getEventType() != OrderEvent.OrderEventType.COMPLETED) {
            return;
        }

        logger.info("接收到订单完成事件: orderId={}", event.getOrder().getOrderId());

        try {
            // 构建消息数据
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("orderId", event.getOrder().getOrderId());
            messageData.put("userId", event.getOrder().getUserId());
            messageData.put("status", event.getOrder().getStatus());
            messageData.put("time", event.getTimestamp());

            // 发送消息通知
            messageService.sendMessage("order.completed", messageData);

        } catch (Exception e) {
            logger.error("处理订单完成事件失败: orderId={}, error={}",
                    event.getOrder().getOrderId(), e.getMessage());
        }
    }
}