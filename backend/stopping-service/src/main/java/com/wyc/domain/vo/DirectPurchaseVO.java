package com.wyc.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("直接购买信息")
public class DirectPurchaseVO {
    @ApiModelProperty("商品项列表")
    private List<OrderCreateVO.OrderItemCreateVO> items;

    @ApiModelProperty("收货地址ID")
    private Long addressId;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("支付方式")
    private String paymentMethod;
}