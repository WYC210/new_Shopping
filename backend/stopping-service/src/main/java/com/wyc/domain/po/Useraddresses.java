package com.wyc.domain.po;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


/**
 * 用户地址对象 useraddresses
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
public class Useraddresses
{
  

    /** 地址ID */
    private Long addressId;

    /** 用户ID */
    private Long userId;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区/县 */
    private String district;

    /** 详细地址 */
    private String detailAddress;

    /** 是否为默认地址 */
    private Integer isDefault;

    /** 是否已删除 */
    private Integer isDeleted;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updatedAt;

}
