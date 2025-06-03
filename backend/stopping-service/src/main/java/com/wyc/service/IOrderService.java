package com.wyc.service;

import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Orderitems;
import com.wyc.domain.vo.OrderDetailVO;
import com.wyc.domain.vo.OrderCreateVO;
import java.util.List;

public interface IOrderService {
    /**
     * 创建订单
     *
     * @param userId        用户ID
     * @param orderCreateVO 订单创建信息
     * @return 订单ID
     */
    Long createOrder(Long userId, OrderCreateVO orderCreateVO);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderDetailVO getOrderDetail(Long orderId);

    /**
     * 获取用户的订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @return 订单列表
     */
    List<OrderDetailVO> getUserOrders(Long userId, String status);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 支付订单
     *
     * @param orderId       订单ID
     * @param userId        用户ID
     * @param paymentMethod 支付方式
     */
    void payOrder(Long orderId, Long userId, String paymentMethod);

    /**
     * 确认收货
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     */
    void confirmReceipt(Long orderId, Long userId);

    /**
     * 删除订单
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     */
    void deleteOrder(Long orderId, Long userId);

    /**
     * 获取订单项列表
     *
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<Orderitems> getOrderItems(Long orderId);
}