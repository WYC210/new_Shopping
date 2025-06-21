package com.wyc.service.strategy.impl;

import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Payments;
import com.wyc.domain.po.Users;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.PaymentsMapper;
import com.wyc.mapper.UsersMapper;
import com.wyc.service.strategy.PaymentStrategy;
import com.wyc.utils.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 简化的支付策略，直接扣减用户余额
 */
@Component
public class SimplePaymentStrategy implements PaymentStrategy {

    private static final Logger logger = LogUtil.getLogger(SimplePaymentStrategy.class);

    @Autowired
    private PaymentsMapper paymentsMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Override
    @Transactional
    public Payments processPayment(Orders order, String paymentMethod) {
        LogUtil.logMethodStart(logger, "processPayment", order.getOrderId(), paymentMethod);

        // 1. 查询用户信息
        Users user = usersMapper.selectById(order.getUserId());
        if (user == null) {
            throw new ServiceException("用户不存在");
        }

        // 2. 检查余额是否足够
        if (user.getBalance().compareTo(order.getTotalAmount()) < 0) {
            throw new ServiceException("余额不足");
        }

        // 3. 扣减用户余额
        usersMapper.updateBalance(user.getUserId(), -order.getTotalAmount().doubleValue());
        logger.info("扣减用户余额: userId={}, amount={}", user.getUserId(), order.getTotalAmount());

        // 4. 创建支付记录
        Payments payment = new Payments();
        payment.setOrderId(order.getOrderId());
        payment.setOrderNumber("ORD" + order.getOrderId());
        payment.setUserId(order.getUserId());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaidAt(new Date());
        payment.setStatus("SUCCESS");
        payment.setTransactionId("PAY" + System.currentTimeMillis());
        payment.setCreatedAt(new Date());
        payment.setUpdatedAt(new Date());

        // 5. 保存支付记录
        paymentsMapper.insert(payment);

        LogUtil.logMethodSuccess(logger, "processPayment", payment.getPaymentId());
        return payment;
    }

    @Override
    public String getPaymentMethod() {
        return "BALANCE";
    }
}