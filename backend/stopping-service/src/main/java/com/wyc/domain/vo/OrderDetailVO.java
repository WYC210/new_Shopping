package com.wyc.domain.vo;

import com.wyc.domain.po.Orderitems;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("订单详情信息")
public class OrderDetailVO {
    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("订单状态")
    private String status;

    @ApiModelProperty("订单总金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("下单时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;

    @ApiModelProperty("订单项列表")
    private List<Orderitems> orderItems;

    @ApiModelProperty("支付信息")
    private PaymentInfoVO paymentInfo;

    @Data
    @ApiModel("支付信息")
    public static class PaymentInfoVO {
        @ApiModelProperty("支付记录ID")
        private Long paymentId;

        @ApiModelProperty("支付方式")
        private String paymentMethod;

        @ApiModelProperty("支付金额")
        private BigDecimal amount;

        @ApiModelProperty("支付时间")
        private Date paidAt;
    }
}