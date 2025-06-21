package com.wyc.service.processor.impl;

import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Products;
import com.wyc.domain.vo.OrderCreateVO;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.OrderitemsMapper;
import com.wyc.mapper.OrdersMapper;
import com.wyc.mapper.ProductsMapper;
import com.wyc.service.ICouponService;
import com.wyc.service.IMessageService;
import com.wyc.service.processor.OrderProcessor;
import com.wyc.utils.LogUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单创建处理器
 */
@Component
public class CreateOrderProcessor implements OrderProcessor {

    private static final Logger logger = LogUtil.getLogger(CreateOrderProcessor.class);

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderitemsMapper orderitemsMapper;

    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private ICouponService couponService;

    @Autowired
    private IMessageService messageService;

    /**
     * 创建订单
     *
     * @param order 订单基本信息
     * @return 创建后的订单
     */
    @Override
    @Transactional
    public Orders process(Orders order) {
        LogUtil.logMethodStart(logger, "process", order);

        // 设置订单状态和时间
        order.setStatus("PENDING");
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());

        // 保存订单
        ordersMapper.insert(order);

        LogUtil.logMethodSuccess(logger, "process", order.getOrderId());
        return order;
    }

    /**
     * 验证订单是否可以创建
     *
     * @param order 订单
     * @return 是否可以创建
     */
    @Override
    public boolean canProcess(Orders order) {
        return order.getOrderId() == null;
    }

    /**
     * 验证商品库存
     *
     * @param itemCreateVO 订单项创建VO
     * @return 商品信息
     */
    public Products validateProductStock(OrderCreateVO.OrderItemCreateVO itemCreateVO) {
        Products product = productsMapper.selectById(itemCreateVO.getProductId());
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        if (product.getStock() < itemCreateVO.getQuantity()) {
            throw new ServiceException("商品库存不足");
        }
        return product;
    }

    /**
     * 计算订单总金额
     *
     * @param items 订单项列表
     * @return 总金额
     */
    public BigDecimal calculateTotalAmount(Iterable<OrderCreateVO.OrderItemCreateVO> items) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderCreateVO.OrderItemCreateVO item : items) {
            Products product = productsMapper.selectById(item.getProductId());
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        return totalAmount;
    }

    /**
     * 应用优惠券折扣
     *
     * @param userId      用户ID
     * @param couponId    优惠券ID
     * @param totalAmount 总金额
     * @return 折扣后金额
     */
    public BigDecimal applyDiscount(Long userId, Long couponId, BigDecimal totalAmount) {
        if (couponId == null) {
            return totalAmount;
        }

        try {
            double discountAmount = couponService.calculateDiscount(userId, couponId, totalAmount.doubleValue());
            if (discountAmount > 0) {
                totalAmount = totalAmount.subtract(new BigDecimal(discountAmount));
                logger.info("应用优惠券折扣: couponId={}, 折扣金额={}, 最终金额={}",
                        couponId, discountAmount, totalAmount);
            }
        } catch (Exception e) {
            logger.error("计算优惠券折扣失败: couponId={}, error={}", couponId, e.getMessage());
        }

        return totalAmount;
    }
}