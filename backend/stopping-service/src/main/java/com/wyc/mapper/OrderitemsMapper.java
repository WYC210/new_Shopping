package com.wyc.mapper;

import com.wyc.domain.po.Orderitems;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderitemsMapper {
    /**
     * 插入订单项
     */
    int insert(Orderitems orderItem);

    /**
     * 根据ID查询订单项
     */
    Orderitems selectById(@Param("orderItemId") Long orderItemId);

    /**
     * 根据订单ID查询订单项列表
     */
    List<Orderitems> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单ID删除订单项
     */
    int deleteByOrderId(@Param("orderId") Long orderId);
}