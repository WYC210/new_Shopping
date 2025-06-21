package com.wyc.domain.builder;

import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Orderitems;
import com.wyc.domain.vo.OrderCreateVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单建造者，用于构建订单及其相关对象
 */
public class OrderBuilder {

    private final Orders order;
    private final List<Orderitems> orderItems = new ArrayList<>();

    private OrderBuilder() {
        this.order = new Orders();
        this.order.setCreatedAt(new Date());
        this.order.setUpdatedAt(new Date());
    }

    /**
     * 创建订单建造者
     * 
     * @return 订单建造者
     */
    public static OrderBuilder create() {
        return new OrderBuilder();
    }

    /**
     * 设置用户ID
     * 
     * @param userId 用户ID
     * @return 订单建造者
     */
    public OrderBuilder withUserId(Long userId) {
        this.order.setUserId(userId);
        return this;
    }

    /**
     * 设置订单状态
     * 
     * @param status 订单状态
     * @return 订单建造者
     */
    public OrderBuilder withStatus(String status) {
        this.order.setStatus(status);
        return this;
    }

    /**
     * 设置订单总金额
     * 
     * @param totalAmount 订单总金额
     * @return 订单建造者
     */
    public OrderBuilder withTotalAmount(BigDecimal totalAmount) {
        this.order.setTotalAmount(totalAmount);
        return this;
    }

    /**
     * 设置优惠券ID
     * 
     * @param couponId 优惠券ID
     * @return 订单建造者
     */
    public OrderBuilder withCouponId(Long couponId) {
        this.order.setCouponId(couponId);
        return this;
    }

    /**
     * 添加订单项
     * 
     * @param productId    商品ID
     * @param productName  商品名称
     * @param quantity     数量
     * @param price        价格
     * @param productImage 商品图片
     * @return 订单建造者
     */
    public OrderBuilder addItem(Long productId, String productName, int quantity,
            BigDecimal price, String productImage) {
        Orderitems item = new Orderitems();
        item.setProductId(productId);
        item.setProductName(productName);
        item.setQuantity((long) quantity);
        item.setPrice(price);
        item.setProductImage(productImage);
        item.setCreatedAt(new Date());

        this.orderItems.add(item);
        return this;
    }

    /**
     * 从订单创建VO中添加订单项
     * 
     * @param createVO 订单创建VO
     * @return 订单建造者
     */
    public OrderBuilder addItemsFromCreateVO(OrderCreateVO createVO) {
        for (OrderCreateVO.OrderItemCreateVO itemVO : createVO.getItems()) {
            Orderitems item = new Orderitems();
            item.setProductId(itemVO.getProductId());
            item.setQuantity((long) itemVO.getQuantity());
            item.setProductSkuId(itemVO.getSkuId());
            item.setCreatedAt(new Date());

            this.orderItems.add(item);
        }
        return this;
    }

    /**
     * 构建订单
     * 
     * @return 订单
     */
    public Orders build() {
        return this.order;
    }

    /**
     * 获取订单项列表
     * 
     * @return 订单项列表
     */
    public List<Orderitems> getOrderItems() {
        return this.orderItems;
    }
}