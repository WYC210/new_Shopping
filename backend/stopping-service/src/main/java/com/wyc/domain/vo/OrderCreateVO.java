package com.wyc.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("订单创建信息")
public class OrderCreateVO {
    @ApiModelProperty("商品项列表")
    private List<OrderItemCreateVO> items;

    @ApiModelProperty("优惠券ID")
    private Long couponId;

    @ApiModelProperty("收货地址ID")
    private Long addressId;

    @ApiModelProperty("备注")
    private String remark;

    @Data
    @ApiModel("订单项创建信息")
    public static class OrderItemCreateVO {
        @ApiModelProperty("商品ID")
        private Long productId;

        @ApiModelProperty("SKU ID")
        private Long skuId;

        @ApiModelProperty("购买数量")
        private Integer quantity;
        
        @ApiModelProperty("商品单价")
        private BigDecimal price;
    }
}