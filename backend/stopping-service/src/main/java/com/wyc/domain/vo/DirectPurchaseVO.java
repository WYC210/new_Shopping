package com.wyc.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 直接购买VO
 */
@Data
@ApiModel("直接购买信息")
public class DirectPurchaseVO {
    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("购买数量")
    private Integer quantity;

    @ApiModelProperty("商品单价")
    private BigDecimal price;

    @ApiModelProperty("收货地址ID")
    private Long addressId;

    @ApiModelProperty("支付方式")
    private String paymentMethod;
}