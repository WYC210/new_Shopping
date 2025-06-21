package com.wyc.service.event;

import com.wyc.domain.po.Orders;
import lombok.Getter;

import java.util.Date;

/**
 * 订单事件，用于订单状态变更的通知
 */
@Getter
public class OrderEvent {

    /**
     * 事件类型
     */
    private final OrderEventType eventType;

    /**
     * 订单
     */
    private final Orders order;

    /**
     * 事件发生时间
     */
    private final Date timestamp;

    /**
     * 附加数据
     */
    private final Object data;

    public OrderEvent(OrderEventType eventType, Orders order) {
        this(eventType, order, null);
    }

    public OrderEvent(OrderEventType eventType, Orders order, Object data) {
        this.eventType = eventType;
        this.order = order;
        this.data = data;
        this.timestamp = new Date();
    }

    /**
     * 订单事件类型
     */
    public enum OrderEventType {
        /**
         * 订单创建
         */
        CREATED,

        /**
         * 订单支付成功
         */
        PAID,

        /**
         * 订单取消
         */
        CANCELLED,

        /**
         * 订单完成
         */
        COMPLETED
    }
}