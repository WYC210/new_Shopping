package com.wyc.service;

import com.wyc.domain.po.Transactions;

import java.util.List;

public interface ITransactionService {
    /**
     * 创建交易记录
     *
     * @param transaction 交易信息
     * @return 交易ID
     */
    Long createTransaction(Transactions transaction);

    /**
     * 更新交易信息
     *
     * @param transaction 交易信息
     */
    void updateTransaction(Transactions transaction);

    /**
     * 删除交易记录
     *
     * @param transactionId 交易ID
     */
    void deleteTransaction(Long transactionId);

    /**
     * 更新交易状态
     *
     * @param transactionId 交易ID
     * @param status        状态
     */
    void updateStatus(Long transactionId, Integer status);

    /**
     * 处理支付成功
     *
     * @param transactionId           交易ID
     * @param thirdPartyTransactionId 第三方支付交易号
     */
    void handlePaymentSuccess(Long transactionId, String thirdPartyTransactionId);

    /**
     * 处理支付失败
     *
     * @param transactionId 交易ID
     * @param reason        失败原因
     */
    void handlePaymentFailure(Long transactionId, String reason);

    /**
     * 处理退款
     *
     * @param transactionId 交易ID
     * @param refundAmount  退款金额
     * @param reason        退款原因
     */
    void handleRefund(Long transactionId, java.math.BigDecimal refundAmount, String reason);

    /**
     * 获取交易详情
     *
     * @param transactionId 交易ID
     * @return 交易信息
     */
    Transactions getTransaction(Long transactionId);

    /**
     * 获取订单交易
     *
     * @param orderId 订单ID
     * @return 交易信息
     */
    Transactions getOrderTransaction(Long orderId);

    /**
     * 获取用户交易列表
     *
     * @param userId 用户ID
     * @return 交易列表
     */
    List<Transactions> getUserTransactions(Long userId);

    /**
     * 获取状态交易列表
     *
     * @param status 状态
     * @return 交易列表
     */
    List<Transactions> getTransactionsByStatus(Integer status);
}