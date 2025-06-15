package com.wyc.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户优惠券详情VO
 *
 * @author wyc
 * @date 2025-06-15
 */
@Data
@ApiModel("用户优惠券详情信息")
public class UserCouponDetailVO {

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("优惠券ID")
    private Long couponId;

    @ApiModelProperty("优惠券名称")
    private String couponName;

    @ApiModelProperty("使用状态（unused-未使用，used-已使用，expired-已过期）")
    private String status;

    @ApiModelProperty("使用时间")
    private Date useTime;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("优惠券类型（DISCOUNT-折扣券，AMOUNT-满减券）")
    private String type;

    @ApiModelProperty("优惠金额或折扣率")
    private BigDecimal value;

    @ApiModelProperty("使用门槛（满多少可用）")
    private BigDecimal threshold;
}