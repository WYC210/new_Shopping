package com.wyc.service.factory;

import com.wyc.exception.ServiceException;
import com.wyc.service.strategy.PaymentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付策略工厂，根据支付方式获取对应的支付策略
 */
@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategyMap = new HashMap<>();

    @Autowired
    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        for (PaymentStrategy strategy : strategies) {
            strategyMap.put(strategy.getPaymentMethod().toUpperCase(), strategy);
        }
    }

    /**
     * 根据支付方式获取支付策略
     * 
     * @param paymentMethod 支付方式
     * @return 支付策略
     */
    public PaymentStrategy getStrategy(String paymentMethod) {
        PaymentStrategy strategy = strategyMap.get(paymentMethod.toUpperCase());
        if (strategy == null) {
            throw new ServiceException("不支持的支付方式: " + paymentMethod);
        }
        return strategy;
    }
}