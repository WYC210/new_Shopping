package com.wyc.service.strategy;

import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Payments;

/**
 * 支付策略接口
 */
public interface PaymentStrategy {

    /**
     * 处理支付
     * 
     * @param order         订单
     * @param paymentMethod 支付方式
     * @return 支付记录
     */
    Payments processPayment(Orders order, String paymentMethod);

    /**
     * 获取支付方式
     * 
     * @return 支付方式
     */
    String getPaymentMethod();
}