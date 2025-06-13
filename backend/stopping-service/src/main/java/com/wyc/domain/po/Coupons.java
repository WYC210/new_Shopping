package com.wyc.domain.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 优惠卷对象 coupons
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
@ApiModel("优惠券信息")
public class Coupons {

    @ApiModelProperty("优惠券ID")
    private Long couponId;

    @ApiModelProperty("优惠券名称")
    private String name;

    @ApiModelProperty("优惠券类型（DISCOUNT-折扣券，AMOUNT-满减券）")
    private String type;

    @ApiModelProperty("优惠金额或折扣率")
    private BigDecimal value;

    @ApiModelProperty("使用门槛（满多少可用）")
    private BigDecimal threshold;

    @ApiModelProperty("有效期开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startTime;

    @ApiModelProperty("有效期结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    @ApiModelProperty("发放数量")
    private Integer totalCount;

    @ApiModelProperty("已领取数量")
    private Integer usedCount;

    @ApiModelProperty("每人限领数量")
    private Integer perLimit;

    @ApiModelProperty("使用说明")
    private String description;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updatedAt;

    /** 优惠券类型：固定金额/百分比折扣/免运费 */
    private String couponType;

    /** 折扣比例(0-100)，仅对百分比折扣券有效 */
    private BigDecimal discountPercentage;

    /** 适用范围：全部/特定分类/特定商品 */
    private String applicableScope;
}
