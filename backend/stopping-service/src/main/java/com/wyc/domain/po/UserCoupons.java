package com.wyc.domain.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 用户优惠卷对象 usercoupons
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
@ApiModel("用户优惠券信息")
public class UserCoupons {
    

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
}
