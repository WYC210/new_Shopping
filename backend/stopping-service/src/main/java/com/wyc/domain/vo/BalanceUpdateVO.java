package com.wyc.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("余额更新请求")
public class BalanceUpdateVO {
    /**
     * 变动金额（正数增加，负数减少）
     */
    @ApiModelProperty("变动金额（正数增加，负数减少）")
    private Double amount;
}