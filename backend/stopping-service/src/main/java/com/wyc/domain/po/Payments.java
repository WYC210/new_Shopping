package com.wyc.domain.po;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


/**
 * 订单支付表对象 payments
 *
 * @author wyc
 * @date 2025-05-30
 */
@Data
public class Payments
{


    /** 支付记录ID */
    private Long paymentId;

    /** 订单ID */

    private Long orderId;

    /** 订单号（冗余，便于支付记录对账） */

    private String orderNumber;

    /** 用户ID */

    private Long userId;

    /** 支付金额 */

    private BigDecimal amount;

    /** 支付方式 */

    private String paymentMethod;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date paidAt;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdAt;

    /** 订单表信息 */
    private List<Orders> ordersList;

}
