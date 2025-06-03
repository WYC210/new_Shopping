package com.wyc.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("用户优惠券信息")
public class UserCouponVO {

    @ApiModelProperty("用户优惠券ID")
    private Long userCouponId;

    @ApiModelProperty("优惠券ID")
    private Long couponId;

    @ApiModelProperty("优惠券名称")
    private String couponName;

    @ApiModelProperty("优惠券类型")
    private String couponType;

    @ApiModelProperty("折扣金额")
    private BigDecimal discountAmount;

    @ApiModelProperty("折扣比例")
    private BigDecimal discountPercentage;

    @ApiModelProperty("适用范围")
    private String applicableScope;

    @ApiModelProperty("使用状态")
    private String status;

    @ApiModelProperty("使用时间")
    private LocalDateTime usedAt;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;
}