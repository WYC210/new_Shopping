package com.wyc.domain.po;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 订单表对象 orders
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
public class Orders {

    /** 订单ID */
    private Long orderId;

    /** 用户ID */
    private Long userId;

    /** 优惠券ID */
    private Long couponId;

    /** 订单状态 */
    private String status;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 下单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updatedAt;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date completedAt;

    /** 订单详细表信息 */
    private List<Orderitems> orderitemsList;

}
