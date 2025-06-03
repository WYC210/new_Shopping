package com.wyc.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("购物车项视图对象")
public class CartItemVO {
    @ApiModelProperty("购物车项ID")
    private Long itemId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("商品数量")
    private Integer quantity;

    @ApiModelProperty("商品价格")
    private BigDecimal price;

    @ApiModelProperty("是否选中")
    private Boolean isSelected;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}