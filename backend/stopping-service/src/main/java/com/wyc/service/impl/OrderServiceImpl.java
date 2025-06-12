package com.wyc.service.impl;

import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Orderitems;
import com.wyc.domain.po.Products;
import com.wyc.domain.po.Payments;
import com.wyc.domain.vo.OrderDetailVO;
import com.wyc.domain.vo.OrderCreateVO;
import com.wyc.domain.vo.OrderDetailVO.PaymentInfoVO;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.OrdersMapper;
import com.wyc.mapper.OrderitemsMapper;
import com.wyc.mapper.ProductsMapper;
import com.wyc.mapper.PaymentsMapper;
import com.wyc.service.IOrderService;
import com.wyc.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderitemsMapper orderitemsMapper;

    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private PaymentsMapper paymentsMapper;

    @Autowired
    private IUserService userService;

    @Override
    @Transactional
    public Long createOrder(Long userId, OrderCreateVO orderCreateVO) {
        // 1. 验证商品库存
        for (OrderCreateVO.OrderItemCreateVO item : orderCreateVO.getItems()) {
            Products product = productsMapper.selectById(item.getProductId());
            if (product == null) {
                throw new ServiceException("商品不存在");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new ServiceException("商品库存不足");
            }
        }

        // 2. 创建订单
        Orders order = new Orders();
        order.setUserId(userId);
        order.setStatus("PENDING"); // 待支付状态
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());

        // 3. 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderCreateVO.OrderItemCreateVO item : orderCreateVO.getItems()) {
            Products product = productsMapper.selectById(item.getProductId());
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        order.setTotalAmount(totalAmount);

        // 4. 保存订单
        ordersMapper.insert(order);

        // 5. 创建订单项
        for (OrderCreateVO.OrderItemCreateVO item : orderCreateVO.getItems()) {
            Products product = productsMapper.selectById(item.getProductId());
            Orderitems orderItem = new Orderitems();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductSkuId(item.getSkuId());
            orderItem.setQuantity(item.getQuantity().longValue());
            orderItem.setPrice(product.getPrice());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImageUrl());
            orderItem.setCreatedAt(new Date());
            orderitemsMapper.insert(orderItem);

            // 6. 扣减库存
            productsMapper.updateStock(item.getProductId(), -item.getQuantity());
        }

        return order.getOrderId();
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        // 1. 获取订单信息
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        // 2. 获取订单项
        List<Orderitems> orderItems = orderitemsMapper.selectByOrderId(orderId);

        // 3. 获取支付信息
        Payments payment = paymentsMapper.selectByOrderId(orderId);

        // 4. 组装订单详情
        OrderDetailVO orderDetail = new OrderDetailVO();
        BeanUtils.copyProperties(order, orderDetail);
        orderDetail.setOrderItems(orderItems);

        if (payment != null) {
            PaymentInfoVO paymentInfo = new PaymentInfoVO();
            paymentInfo.setPaymentId(payment.getPaymentId());
            paymentInfo.setPaymentMethod(payment.getPaymentMethod());
            paymentInfo.setAmount(payment.getAmount());
            paymentInfo.setPaidAt(payment.getPaidAt());
            orderDetail.setPaymentInfo(paymentInfo);
        }

        return orderDetail;
    }

    @Override
    public List<OrderDetailVO> getUserOrders(Long userId, String status) {
        List<Orders> orders = ordersMapper.selectUserOrders(userId, status);
        return orders.stream()
                .map(order -> getOrderDetail(order.getOrderId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        // 1. 验证订单
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }
        if (!"pending".equals(order.getStatus())) {
            throw new ServiceException("只能取消待支付的订单");
        }

        // 2. 恢复库存
        List<Orderitems> orderItems = orderitemsMapper.selectByOrderId(orderId);
        for (Orderitems item : orderItems) {
            productsMapper.updateStock(item.getProductId(), item.getQuantity().intValue());
        }

        // 3. 更新订单状态
        order.setStatus("CANCELLED");
        order.setUpdatedAt(new Date());
        ordersMapper.updateById(order);
    }

    @Override
    @Transactional
    public void payOrder(Long orderId, Long userId, String paymentMethod) {
        // 1. 验证订单
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }
        if (!"pending".equals(order.getStatus())) {
            throw new ServiceException("订单状态不正确");
        }

        // 2. 创建支付记录
        Payments payment = new Payments();
        payment.setOrderId(orderId);
        payment.setOrderNumber(String.valueOf(orderId)); // 实际应该生成订单号
        payment.setUserId(userId);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaidAt(new Date());
        payment.setCreatedAt(new Date());
        paymentsMapper.insert(payment);

        // 3. 更新订单状态
        order.setStatus("PAID");
        order.setUpdatedAt(new Date());
        ordersMapper.updateById(order);

        // 注意：直接购买不需要删除购物车项
    }

    @Override
    @Transactional
    public void confirmReceipt(Long orderId, Long userId) {
        // 1. 验证订单
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new ServiceException("订单状态不正确");
        }

        // 2. 更新订单状态
        order.setStatus("COMPLETED");
        order.setUpdatedAt(new Date());
        ordersMapper.updateById(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        // 1. 验证订单
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }
        if (!"COMPLETED".equals(order.getStatus()) && !"CANCELLED".equals(order.getStatus())) {
            throw new ServiceException("只能删除已完成或已取消的订单");
        }

        // 2. 删除订单项
        orderitemsMapper.deleteByOrderId(orderId);

        // 3. 删除支付记录
        paymentsMapper.deleteByOrderId(orderId);

        // 4. 删除订单
        ordersMapper.deleteById(orderId);
    }

    @Override
    public List<Orderitems> getOrderItems(Long orderId) {
        return orderitemsMapper.selectByOrderId(orderId);
    }
}
