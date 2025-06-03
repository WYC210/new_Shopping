package com.wyc.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@ApiModel("浏览记录信息")
public class BrowsingRecordVO {

    @ApiModelProperty("记录ID")
    private Long recordId;

    @ApiModelProperty("商品ID")
    private Long productId;

    @ApiModelProperty("商品名称")
    private String productName;

    @ApiModelProperty("浏览时间")
    private LocalDateTime viewedAt;

    public BrowsingRecordVO(Long productId, String productName, Date viewedAt) {
        this.productId = productId;
        this.productName = productName;
        if (viewedAt != null) {
            this.viewedAt = viewedAt instanceof java.sql.Timestamp
                    ? ((java.sql.Timestamp) viewedAt).toLocalDateTime()
                    : new java.sql.Timestamp(viewedAt.getTime()).toLocalDateTime();
        }
    }
}