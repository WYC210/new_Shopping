package com.wyc.domain.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 购物车项对象 cartitems
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
@ApiModel("购物车项信息")
public class CartItems {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("购物车项ID")
    private Long itemId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("SKU ID")
    private Long skuId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("商品数量")
    private Integer quantity;

    @ApiModelProperty("是否选中（0-未选中，1-已选中）")
    private Boolean isSelected;

    @ApiModelProperty("是否已删除（0-未删除，1-已删除）")
    private Boolean isDeleted;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}