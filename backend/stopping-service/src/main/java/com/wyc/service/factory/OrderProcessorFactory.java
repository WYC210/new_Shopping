package com.wyc.service.factory;

import com.wyc.domain.po.Orders;
import com.wyc.service.processor.OrderProcessor;
import com.wyc.service.processor.impl.CancelOrderProcessor;
import com.wyc.service.processor.impl.CompleteOrderProcessor;
import com.wyc.service.processor.impl.CreateOrderProcessor;
import com.wyc.service.processor.impl.PayOrderProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单处理器工厂，根据订单状态或操作类型创建对应的处理器
 */
@Component
public class OrderProcessorFactory {

    private final Map<String, OrderProcessor> processorMap = new HashMap<>();

    @Autowired
    public OrderProcessorFactory(CreateOrderProcessor createOrderProcessor,
            PayOrderProcessor payOrderProcessor,
            CancelOrderProcessor cancelOrderProcessor,
            CompleteOrderProcessor completeOrderProcessor) {
        processorMap.put("create", createOrderProcessor);
        processorMap.put("pay", payOrderProcessor);
        processorMap.put("cancel", cancelOrderProcessor);
        processorMap.put("complete", completeOrderProcessor);
    }

    /**
     * 根据操作类型获取订单处理器
     *
     * @param operationType 操作类型
     * @return 订单处理器
     */
    public OrderProcessor getProcessor(String operationType) {
        return processorMap.get(operationType.toLowerCase());
    }

    /**
     * 根据订单状态获取下一步处理器
     *
     * @param order 订单
     * @return 订单处理器
     */
    public OrderProcessor getProcessorByOrderStatus(Orders order) {
        String status = order.getStatus().toLowerCase();
        switch (status) {
            case "pending":
                return processorMap.get("pay");
            case "pay":
                return processorMap.get("complete");
            default:
                return null;
        }
    }
}