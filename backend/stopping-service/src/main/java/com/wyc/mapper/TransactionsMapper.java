package com.wyc.mapper;

import com.wyc.domain.po.Transactions;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionsMapper {
    /**
     * 插入交易记录
     */
    int insert(Transactions transaction);

    /**
     * 根据ID查询交易
     */
    Transactions selectById(@Param("transactionId") Long transactionId);

    /**
     * 根据订单ID查询交易
     */
    Transactions selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据用户ID查询交易列表
     */
    List<Transactions> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据状态查询交易列表
     */
    List<Transactions> selectByStatus(@Param("status") Integer status);

    /**
     * 更新交易信息
     */
    int updateById(Transactions transaction);

    /**
     * 更新交易状态
     */
    int updateStatus(@Param("transactionId") Long transactionId, @Param("status") Integer status);

    /**
     * 更新退款信息
     */
    int updateRefund(@Param("transactionId") Long transactionId,
            @Param("refundAmount") java.math.BigDecimal refundAmount,
            @Param("refundReason") String refundReason,
            @Param("refundTime") java.util.Date refundTime);

    /**
     * 删除交易记录
     */
    int deleteById(@Param("transactionId") Long transactionId);
}