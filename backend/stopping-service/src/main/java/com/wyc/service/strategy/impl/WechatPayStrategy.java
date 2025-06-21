package com.wyc.service.strategy.impl;

import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Payments;
import com.wyc.mapper.PaymentsMapper;
import com.wyc.service.strategy.PaymentStrategy;
import com.wyc.utils.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 微信支付策略
 */
@Component
public class WechatPayStrategy implements PaymentStrategy {

    private static final Logger logger = LogUtil.getLogger(WechatPayStrategy.class);

    @Autowired
    private PaymentsMapper paymentsMapper;

    @Override
    public Payments processPayment(Orders order, String paymentMethod) {
        LogUtil.logMethodStart(logger, "processPayment", order.getOrderId(), paymentMethod);

        // 创建支付记录
        Payments payment = new Payments();
        payment.setOrderId(order.getOrderId());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod("WECHAT");
        payment.setPaidAt(new Date());

        // 这里可以调用微信支付API进行实际支付处理
        // 模拟支付成功
        payment.setTransactionId("WX" + System.currentTimeMillis());

        // 保存支付记录
        paymentsMapper.insert(payment);

        LogUtil.logMethodSuccess(logger, "processPayment", payment.getPaymentId());
        return payment;
    }

    @Override
    public String getPaymentMethod() {
        return "WECHAT";
    }
}