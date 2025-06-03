package com.wyc.domain.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(description = "支付交易")
public class Transactions {
    @ApiModelProperty("交易ID")
    private Long transactionId;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("交易金额")
    private BigDecimal amount;

    @ApiModelProperty("支付方式：1-支付宝，2-微信支付，3-银行卡")
    private Integer paymentMethod;

    @ApiModelProperty("交易状态：0-待支付，1-支付成功，2-支付失败，3-已退款")
    private Integer status;

    @ApiModelProperty("第三方支付交易号")
    private String thirdPartyTransactionId;

    @ApiModelProperty("支付时间")
    private Date paymentTime;

    @ApiModelProperty("退款时间")
    private Date refundTime;

    @ApiModelProperty("退款金额")
    private BigDecimal refundAmount;

    @ApiModelProperty("退款原因")
    private String refundReason;

    @ApiModelProperty("交易备注")
    private String remark;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}