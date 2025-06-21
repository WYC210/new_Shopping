package com.wyc.service.impl;

import com.wyc.domain.builder.OrderBuilder;
import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Orderitems;
import com.wyc.domain.po.Products;
import com.wyc.domain.po.Payments;
import com.wyc.domain.po.Users;
import com.wyc.domain.vo.OrderCreateVO;
import com.wyc.domain.vo.OrderDetailVO;
import com.wyc.domain.vo.OrderListVO;
import com.wyc.domain.vo.DirectPurchaseVO;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.OrderitemsMapper;
import com.wyc.mapper.OrdersMapper;
import com.wyc.mapper.ProductsMapper;
import com.wyc.mapper.PaymentsMapper;
import com.wyc.mapper.UsersMapper;
import com.wyc.service.IOrderService;
import com.wyc.service.event.OrderEventPublisher;
import com.wyc.service.factory.OrderProcessorFactory;
import com.wyc.service.factory.PaymentStrategyFactory;
import com.wyc.service.processor.OrderProcessor;
import com.wyc.service.strategy.PaymentStrategy;
import com.wyc.service.ICouponService;
import com.wyc.utils.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl implements IOrderService {

    private static final Logger logger = LogUtil.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderitemsMapper orderitemsMapper;

    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private PaymentsMapper paymentsMapper;

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private OrderProcessorFactory orderProcessorFactory;

    @Autowired
    private PaymentStrategyFactory paymentStrategyFactory;

    @Autowired
    private OrderEventPublisher eventPublisher;

    @Autowired
    private ICouponService couponService;

    @Override
    @Transactional
    public Long createOrder(Long userId, OrderCreateVO orderCreateVO) {
        LogUtil.logMethodStart(logger, "createOrder", userId, orderCreateVO);

        // 1. 验证商品库存
        validateProductStock(orderCreateVO);

        // 2. 使用建造者模式创建订单
        OrderBuilder orderBuilder = OrderBuilder.create()
                .withUserId(userId)
                .withStatus("PENDING");

        // 3. 设置优惠券
        if (orderCreateVO.getCouponId() != null) {
            orderBuilder.withCouponId(orderCreateVO.getCouponId());
        }

        // 4. 计算订单总金额
        BigDecimal totalAmount = calculateTotalAmount(orderCreateVO);
        orderBuilder.withTotalAmount(totalAmount);

        // 5. 构建订单并保存
        Orders order = orderBuilder.build();

        // 6. 使用订单处理器处理订单创建
        OrderProcessor processor = orderProcessorFactory.getProcessor("create");
        order = processor.process(order);

        // 7. 创建订单项
        createOrderItems(order.getOrderId(), orderCreateVO);

        // 8. 发布订单创建事件
        eventPublisher.publishOrderCreatedEvent(order);

        LogUtil.logMethodSuccess(logger, "createOrder", order.getOrderId());
        return order.getOrderId();
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        LogUtil.logMethodStart(logger, "getOrderDetail", orderId);

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
            OrderDetailVO.PaymentInfoVO paymentInfo = new OrderDetailVO.PaymentInfoVO();
            paymentInfo.setPaymentId(payment.getPaymentId());
            paymentInfo.setPaymentMethod(payment.getPaymentMethod());
            paymentInfo.setAmount(payment.getAmount());
            paymentInfo.setPaidAt(payment.getPaidAt());
            orderDetail.setPaymentInfo(paymentInfo);
        }

        LogUtil.logMethodSuccess(logger, "getOrderDetail", orderDetail);
        return orderDetail;
    }

    @Override
    public List<OrderDetailVO> getUserOrders(Long userId, String status) {
        LogUtil.logMethodStart(logger, "getUserOrders", userId, status);

        List<Orders> orders = ordersMapper.selectUserOrders(userId, status);
        List<OrderDetailVO> result = orders.stream()
                .map(order -> getOrderDetail(order.getOrderId()))
                .collect(Collectors.toList());

        LogUtil.logMethodSuccess(logger, "getUserOrders", result.size());
        return result;
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        LogUtil.logMethodStart(logger, "cancelOrder", orderId, userId);

        // 1. 验证订单
        Orders order = validateOrderOwnership(orderId, userId);

        // 2. 使用订单处理器处理取消
        OrderProcessor processor = orderProcessorFactory.getProcessor("cancel");
        if (!processor.canProcess(order)) {
            throw new ServiceException("当前订单状态不允许取消");
        }

        order = processor.process(order);

        // 3. 发布订单取消事件
        eventPublisher.publishOrderCancelledEvent(order);

        LogUtil.logMethodSuccess(logger, "cancelOrder", orderId);
    }

    @Override
    @Transactional
    public void payOrder(Long orderId, Long userId, String paymentMethod) {
        LogUtil.logMethodStart(logger, "payOrder", orderId, userId, paymentMethod);

        // 1. 验证订单
        Orders order = validateOrderOwnership(orderId, userId);

        // 2. 使用支付策略处理支付（简化为余额支付）
        PaymentStrategy paymentStrategy = paymentStrategyFactory.getStrategy("BALANCE");
        Payments payment = paymentStrategy.processPayment(order, "BALANCE");

        // 3. 使用订单处理器处理支付后状态更新
        OrderProcessor processor = orderProcessorFactory.getProcessor("pay");
        if (!processor.canProcess(order)) {
            throw new ServiceException("当前订单状态不允许支付");
        }

        order = processor.process(order);

        // 4. 发布订单支付成功事件
        eventPublisher.publishOrderPaidEvent(order, payment);

        // 5. 如果订单使用了优惠券，更新优惠券状态为已使用
        if (order.getCouponId() != null) {
            couponService.useCoupon(userId, order.getCouponId(), orderId);
            logger.info("更新优惠券状态为已使用: userId={}, couponId={}, orderId={}",
                    userId, order.getCouponId(), orderId);
        }

        LogUtil.logMethodSuccess(logger, "payOrder", orderId);
    }

    @Override
    @Transactional
    public void confirmReceipt(Long orderId, Long userId) {
        LogUtil.logMethodStart(logger, "confirmReceipt", orderId, userId);

        // 1. 验证订单
        Orders order = validateOrderOwnership(orderId, userId);

        // 2. 使用订单处理器处理确认收货
        OrderProcessor processor = orderProcessorFactory.getProcessor("complete");
        if (!processor.canProcess(order)) {
            throw new ServiceException("当前订单状态不允许确认收货");
        }

        order = processor.process(order);

        // 3. 发布订单完成事件
        eventPublisher.publishOrderCompletedEvent(order);

        LogUtil.logMethodSuccess(logger, "confirmReceipt", orderId);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        LogUtil.logMethodStart(logger, "deleteOrder", orderId, userId);

        // 验证订单
        Orders order = validateOrderOwnership(orderId, userId);

        // 只允许删除已完成或已取消的订单
        if (!("COMPLETED".equalsIgnoreCase(order.getStatus()) || "CANCELLED".equalsIgnoreCase(order.getStatus()))) {
            throw new ServiceException("只能删除已完成或已取消的订单");
        }

        // 标记订单为已删除（逻辑删除）
        ordersMapper.markAsDeleted(orderId);

        LogUtil.logMethodSuccess(logger, "deleteOrder", orderId);
    }

    @Override
    public List<Orderitems> getOrderItems(Long orderId) {
        return orderitemsMapper.selectByOrderId(orderId);
    }

    @Override
    public Orders getOrder(Long orderId) {
        return ordersMapper.selectById(orderId);
    }

    @Override
    public Map<String, Object> getUserOrdersWithPagination(Long userId, String status, int page, int pageSize) {
        LogUtil.logMethodStart(logger, "getUserOrdersWithPagination", userId, status, page, pageSize);

        // 手动实现分页
        int offset = (page - 1) * pageSize;
        List<Orders> orders = ordersMapper.selectUserOrdersPaged(userId, status, offset, pageSize);
        int totalCount = ordersMapper.countUserOrders(userId, status);
        Long total = Long.valueOf(totalCount);

        List<OrderListVO> orderList = orders.stream().map(order -> {
            OrderListVO vo = new OrderListVO();
            BeanUtils.copyProperties(order, vo);

            // 获取订单项数量
            List<Orderitems> items = orderitemsMapper.selectByOrderId(order.getOrderId());
            vo.setItemCount(items.size());

            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", orderList);
        result.put("page", page);
        result.put("pageSize", pageSize);

        LogUtil.logMethodSuccess(logger, "getUserOrdersWithPagination", result);
        return result;
    }

    /**
     * 验证商品库存
     *
     * @param orderCreateVO 订单创建VO
     */
    private void validateProductStock(OrderCreateVO orderCreateVO) {
        for (OrderCreateVO.OrderItemCreateVO item : orderCreateVO.getItems()) {
            Products product = productsMapper.selectById(item.getProductId());
            if (product == null) {
                throw new ServiceException("商品不存在");
            }
            if (product.getStock() < item.getQuantity()) {
                throw new ServiceException("商品库存不足");
            }
        }
    }

    /**
     * 计算订单总金额
     *
     * @param orderCreateVO 订单创建VO
     * @return 总金额
     */
    private BigDecimal calculateTotalAmount(OrderCreateVO orderCreateVO) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderCreateVO.OrderItemCreateVO item : orderCreateVO.getItems()) {
            Products product = productsMapper.selectById(item.getProductId());
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        return totalAmount;
    }

    /**
     * 创建订单项
     *
     * @param orderId       订单ID
     * @param orderCreateVO 订单创建VO
     */
    private void createOrderItems(Long orderId, OrderCreateVO orderCreateVO) {
        for (OrderCreateVO.OrderItemCreateVO item : orderCreateVO.getItems()) {
            Products product = productsMapper.selectById(item.getProductId());

            Orderitems orderItem = new Orderitems();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(item.getProductId());
            orderItem.setProductSkuId(item.getSkuId());
            orderItem.setQuantity(item.getQuantity().longValue());
            orderItem.setPrice(product.getPrice());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImageUrl());
            orderItem.setCreatedAt(new java.util.Date());

            orderitemsMapper.insert(orderItem);

            // 扣减库存
            productsMapper.updateStock(item.getProductId(), -item.getQuantity());
        }
    }

    /**
     * 验证订单所有权
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 订单
     */
    private Orders validateOrderOwnership(Long orderId, Long userId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }
        return order;
    }
}