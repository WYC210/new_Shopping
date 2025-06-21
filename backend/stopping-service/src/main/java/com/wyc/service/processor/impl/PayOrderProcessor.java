package com.wyc.service.processor.impl;

import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Payments;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.OrdersMapper;
import com.wyc.mapper.PaymentsMapper;
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
 * 订单支付处理器
 */
@Component
public class PayOrderProcessor implements OrderProcessor {

    private static final Logger logger = LogUtil.getLogger(PayOrderProcessor.class);

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private PaymentsMapper paymentsMapper;

    @Autowired
    private IMessageService messageService;

    /**
     * 处理订单支付
     *
     * @param order 订单
     * @return 处理后的订单
     */
    @Override
    @Transactional
    public Orders process(Orders order) {
        LogUtil.logMethodStart(logger, "process", order);

        // 更新订单状态
        order.setStatus("PAID");
        order.setUpdatedAt(new Date());
        ordersMapper.updateById(order);

        // 发送支付成功消息
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("orderId", order.getOrderId());
        messageData.put("userId", order.getUserId());
        messageData.put("status", order.getStatus());
        messageData.put("amount", order.getTotalAmount());
        messageData.put("time", new Date());

        try {
            messageService.sendMessage("order.payment.success", messageData);
            logger.info("发送支付成功消息: orderId={}", order.getOrderId());
        } catch (Exception e) {
            logger.error("发送支付成功消息失败: orderId={}, error={}", order.getOrderId(), e.getMessage());
        }

        LogUtil.logMethodSuccess(logger, "process", order.getOrderId());
        return order;
    }

    /**
     * 验证订单是否可以支付
     *
     * @param order 订单
     * @return 是否可以支付
     */
    @Override
    public boolean canProcess(Orders order) {
        return order != null && "PENDING".equalsIgnoreCase(order.getStatus());
    }

    /**
     * 创建支付记录
     *
     * @param order         订单
     * @param paymentMethod 支付方式
     * @return 支付记录
     */
    public Payments createPayment(Orders order, String paymentMethod) {
        Payments payment = new Payments();
        payment.setOrderId(order.getOrderId());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaidAt(new Date());
        payment.setStatus("SUCCESS");
        paymentsMapper.insert(payment);
        return payment;
    }
}