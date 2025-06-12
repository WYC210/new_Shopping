package com.wyc.mapper;

import com.wyc.domain.po.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrdersMapper {
    /**
     * 插入订单
     */
    int insert(Orders order);

    /**
     * 根据ID查询订单
     */
    Orders selectById(@Param("orderId") Long orderId);

    /**
     * 根据用户ID查询订单列表
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId}")
    List<Orders> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和订单状态查询订单列表
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} AND status = #{status}")
    List<Orders> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 根据用户ID和订单状态查询订单列表（动态SQL，status可为null）
     */
    List<Orders> selectUserOrders(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 根据用户ID和订单状态查询订单总数
     */
    int countUserOrders(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 分页查询用户订单列表
     */
    List<Orders> selectUserOrdersPaged(@Param("userId") Long userId, @Param("status") String status,
            @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 更新订单
     */
    int updateById(Orders order);

    /**
     * 更新订单状态
     */
    int updateStatus(@Param("orderId") Long orderId, @Param("status") String status);

    /**
     * 删除订单
     */
    int deleteById(@Param("orderId") Long orderId);
}