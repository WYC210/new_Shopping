package com.wyc.service.processor.impl;

import com.wyc.domain.po.Orders;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.OrdersMapper;
import com.wyc.service.IMessageService;
import com.wyc.service.processor.OrderProcessor;
import com.wyc.utils.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单完成处理器
 */
@Component
public class CompleteOrderProcessor implements OrderProcessor {

    private static final Logger logger = LogUtil.getLogger(CompleteOrderProcessor.class);

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private IMessageService messageService;

    /**
     * 处理订单完成
     *
     * @param order 订单
     * @return 处理后的订单
     */
    @Override
    @Transactional
    public Orders process(Orders order) {
        LogUtil.logMethodStart(logger, "process", order);

        // 更新订单状态
        order.setStatus("COMPLETED");
        // 设置完成时间
        Date completedAt = new Date();
        order.setCompletedAt(completedAt);
        order.setUpdatedAt(new Date());
        ordersMapper.updateById(order);

        // 发送订单完成消息
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("orderId", order.getOrderId());
        messageData.put("userId", order.getUserId());
        messageData.put("status", order.getStatus());
        messageData.put("completedAt", completedAt);

        try {
            messageService.sendMessage("order.completed", messageData);
            logger.info("发送订单完成消息: orderId={}", order.getOrderId());
        } catch (Exception e) {
            logger.error("发送订单完成消息失败: orderId={}, error={}",
                    order.getOrderId(), e.getMessage());
        }

        LogUtil.logMethodSuccess(logger, "process", order.getOrderId());
        return order;
    }

    /**
     * 验证订单是否可以完成
     *
     * @param order 订单
     * @return 是否可以完成
     */
    @Override
    public boolean canProcess(Orders order) {
        return order != null && "PAY".equalsIgnoreCase(order.getStatus());
    }
}