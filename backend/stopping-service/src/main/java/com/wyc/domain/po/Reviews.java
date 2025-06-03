package com.wyc.domain.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 商品评论对象 reviews
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
@ApiModel("商品评价信息")
public class Reviews {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("评价ID")
    private Long reviewId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("订单ID")
    private Long orderId;

    @ApiModelProperty("订单项ID")
    private Long orderItemId;

    @ApiModelProperty("评分（1-5）")
    private Integer rating;

    @ApiModelProperty("评价内容")
    private String content;

    @ApiModelProperty("评价图片URL，多个图片用逗号分隔")
    private String images;

    @ApiModelProperty("是否匿名（0-否，1-是）")
    private Integer isAnonymous;

    @ApiModelProperty("商家回复")
    private String reply;

    @ApiModelProperty("回复时间")
    private Date replyTime;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新时间")
    private Date updatedAt;
}
