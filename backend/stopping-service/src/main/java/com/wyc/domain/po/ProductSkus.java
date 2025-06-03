package com.wyc.domain.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Data
@ApiModel("商品SKU信息")
public class ProductSkus {
    @ApiModelProperty("SKU ID")
    private Long skuId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("SKU编码")
    private String skuCode;

    @ApiModelProperty("规格属性（如颜色、尺码）")
    private String attributes; // 存储JSON字符串

    @ApiModelProperty("SKU价格")
    private BigDecimal price;

    @ApiModelProperty("SKU库存")
    private Integer stock;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}