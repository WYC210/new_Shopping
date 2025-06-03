package com.wyc.mapper;

import com.wyc.domain.po.Payments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentsMapper {
    /**
     * 插入支付记录
     */
    int insert(Payments payment);

    /**
     * 根据订单ID查询支付记录
     */
    Payments selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单ID删除支付记录
     */
    int deleteByOrderId(@Param("orderId") Long orderId);
}