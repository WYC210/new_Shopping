package com.wyc.service.processor.impl;

import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Orderitems;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.OrderitemsMapper;
import com.wyc.mapper.OrdersMapper;
import com.wyc.mapper.ProductsMapper;
import com.wyc.service.IMessageService;
import com.wyc.service.processor.OrderProcessor;
import com.wyc.utils.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单取消处理器
 */
@Component
public class CancelOrderProcessor implements OrderProcessor {

    private static final Logger logger = LogUtil.getLogger(CancelOrderProcessor.class);

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderitemsMapper orderitemsMapper;

    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private IMessageService messageService;

    /**
     * 处理订单取消
     *
     * @param order 订单
     * @return 处理后的订单
     */
    @Override
    @Transactional
    public Orders process(Orders order) {
        LogUtil.logMethodStart(logger, "process", order);

        // 更新订单状态
        order.setStatus("CANCELLED");
        order.setUpdatedAt(new Date());
        ordersMapper.updateById(order);

        // 恢复商品库存
        restoreProductStock(order.getOrderId());

        // 发送订单取消消息
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("orderId", order.getOrderId());
        messageData.put("userId", order.getUserId());
        messageData.put("status", order.getStatus());
        messageData.put("time", new Date());

        try {
            messageService.sendMessage("order.cancel", messageData);
            logger.info("发送订单取消消息: orderId={}", order.getOrderId());
        } catch (Exception e) {
            logger.error("发送订单取消消息失败: orderId={}, error={}",
                    order.getOrderId(), e.getMessage());
        }

        LogUtil.logMethodSuccess(logger, "process", order.getOrderId());
        return order;
    }

    /**
     * 验证订单是否可以取消
     *
     * @param order 订单
     * @return 是否可以取消
     */
    @Override
    public boolean canProcess(Orders order) {
        return order != null && ("PENDING".equalsIgnoreCase(order.getStatus())
                || "PAY".equalsIgnoreCase(order.getStatus()));
    }

    /**
     * 恢复商品库存
     *
     * @param orderId 订单ID
     */
    private void restoreProductStock(Long orderId) {
        List<Orderitems> orderItems = orderitemsMapper.selectByOrderId(orderId);
        for (Orderitems item : orderItems) {
            productsMapper.updateStock(item.getProductId(), item.getQuantity().intValue());
            logger.info("恢复商品库存: productId={}, quantity={}",
                    item.getProductId(), item.getQuantity());
        }
    }
}