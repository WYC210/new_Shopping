package com.wyc.domain.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 用户地址对象 addresses
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
@ApiModel("用户地址信息")
public class Addresses {


    @ApiModelProperty("地址ID")
    private Long addressId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("收货人姓名")
    private String receiverName;

    @ApiModelProperty("收货人手机号")
    private String receiverPhone;

    @ApiModelProperty("省份")
    private String province;

    @ApiModelProperty("城市")
    private String city;

    @ApiModelProperty("区县")
    private String district;

    @ApiModelProperty("详细地址")
    private String detailAddress;

    @ApiModelProperty("邮政编码")
    private String postalCode;

    @ApiModelProperty("是否默认地址（0-否，1-是）")
    private Integer isDefault;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}