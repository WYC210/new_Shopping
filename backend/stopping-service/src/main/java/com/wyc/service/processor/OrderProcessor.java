package com.wyc.service.processor;

import com.wyc.domain.po.Orders;

/**
 * 订单处理器接口，定义订单处理的通用方法
 */
public interface OrderProcessor {

    /**
     * 处理订单
     * 
     * @param order 订单
     * @return 处理后的订单
     */
    Orders process(Orders order);

    /**
     * 验证订单是否可以被当前处理器处理
     * 
     * @param order 订单
     * @return 是否可以处理
     */
    boolean canProcess(Orders order);
}