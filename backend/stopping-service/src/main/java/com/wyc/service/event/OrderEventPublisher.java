package com.wyc.service.event;

import com.wyc.domain.po.Orders;
import com.wyc.utils.LogUtil;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 订单事件发布器，用于发布订单相关事件
 */
@Component
public class OrderEventPublisher {

    private static final Logger logger = LogUtil.getLogger(OrderEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public OrderEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 发布订单创建事件
     * 
     * @param order 订单
     */
    public void publishOrderCreatedEvent(Orders order) {
        OrderEvent event = new OrderEvent(OrderEvent.OrderEventType.CREATED, order);
        eventPublisher.publishEvent(event);
        logger.info("发布订单创建事件: orderId={}", order.getOrderId());
    }

    /**
     * 发布订单支付成功事件
     * 
     * @param order       订单
     * @param paymentData 支付数据
     */
    public void publishOrderPaidEvent(Orders order, Object paymentData) {
        OrderEvent event = new OrderEvent(OrderEvent.OrderEventType.PAID, order, paymentData);
        eventPublisher.publishEvent(event);
        logger.info("发布订单支付成功事件: orderId={}", order.getOrderId());
    }

    /**
     * 发布订单取消事件
     * 
     * @param order 订单
     */
    public void publishOrderCancelledEvent(Orders order) {
        OrderEvent event = new OrderEvent(OrderEvent.OrderEventType.CANCELLED, order);
        eventPublisher.publishEvent(event);
        logger.info("发布订单取消事件: orderId={}", order.getOrderId());
    }

    /**
     * 发布订单完成事件
     * 
     * @param order 订单
     */
    public void publishOrderCompletedEvent(Orders order) {
        OrderEvent event = new OrderEvent(OrderEvent.OrderEventType.COMPLETED, order);
        eventPublisher.publishEvent(event);
        logger.info("发布订单完成事件: orderId={}", order.getOrderId());
    }
}