package com.wyc.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Token刷新请求")
public class TokenRefreshVO {

    @ApiModelProperty("旧token")
    private String oldToken;

    @ApiModelProperty("设备ID")
    private String deviceId;
}