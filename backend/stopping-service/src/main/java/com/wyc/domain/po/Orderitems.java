package com.wyc.domain.po;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;



/**
 * 订单详细表对象 orderitems
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
public class Orderitems
{


    /** 订单项ID */
    private Long orderItemId;

    /** 订单ID */

    private Long orderId;

    /** 商品ID */

    private Long productId;

    /** SKU ID */

    private Long productSkuId;

    /** 购买数量 */

    private Long quantity;

    /** 购买时单价 */

    private BigDecimal price;

    /** 下单时商品名称（冗余字段，避免关联Products） */

    private String productName;

    /** 下单时商品封面图地址（冗余字段，便于订单页展示） */

    private String productImage;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")

    private Date createdAt;

}
