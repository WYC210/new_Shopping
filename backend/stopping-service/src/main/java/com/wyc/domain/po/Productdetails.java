package com.wyc.domain.po;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


/**
 * 商品详细表对象 productdetails
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
public class Productdetails
{
  

    /** 商品ID */
    private Long productId;

    /** 商品描述 */
    private String description;

    /** 规格参数JSON */
    private String specifications;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updatedAt;


}
