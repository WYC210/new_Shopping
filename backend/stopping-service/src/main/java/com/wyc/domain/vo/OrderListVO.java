package com.wyc.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单列表项VO
 */
@Data
@ApiModel("订单列表项")
public class OrderListVO {
    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("订单状态")
    private String status;

    @ApiModelProperty("订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("商品数量")
    private Integer itemCount;

    @ApiModelProperty("下单时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}