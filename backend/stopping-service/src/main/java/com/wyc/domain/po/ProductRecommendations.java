package com.wyc.domain.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 商品推荐对象 product_recommendations
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
@ApiModel("商品推荐信息")
public class ProductRecommendations {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("推荐ID")
    private Long recommendationId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("推荐类型（1-首页推荐，2-分类推荐，3-相关推荐，4-个性化推荐）")
    private Integer type;

    @ApiModelProperty("推荐位置")
    private String position;

    @ApiModelProperty("排序权重")
    private Integer weight;

    @ApiModelProperty("推荐理由")
    private String reason;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("状态（0-禁用，1-启用）")
    private Integer status;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}