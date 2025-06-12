package com.wyc.service.impl;

import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Transactions;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.OrdersMapper;
import com.wyc.mapper.TransactionsMapper;
import com.wyc.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class TransactionServiceImpl implements ITransactionService {

    @Autowired
    private TransactionsMapper transactionsMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    @Transactional
    public Long createTransaction(Transactions transaction) {
        // 1. 验证订单是否存在
        Orders order = ordersMapper.selectById(transaction.getOrderId());
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        System.out.println("订单状态：" + order.getStatus());
        // 2. 验证订单状态
        if (order.getStatus().equals("pending")) {
            throw new ServiceException("订单状态不正确");
        }

        // 3. 设置创建和更新时间
        transaction.setCreatedAt(new Date());
        transaction.setUpdatedAt(new Date());
        transaction.setStatus(0); // 待支付状态

        // 4. 保存交易记录
        transactionsMapper.insert(transaction);
        return transaction.getTransactionId();
    }

    @Override
    @Transactional
    public void updateTransaction(Transactions transaction) {
        // 1. 验证交易是否存在
        Transactions existingTransaction = transactionsMapper.selectById(transaction.getTransactionId());
        if (existingTransaction == null) {
            throw new ServiceException("交易不存在");
        }

        // 2. 设置更新时间
        transaction.setUpdatedAt(new Date());

        // 3. 更新交易信息
        transactionsMapper.updateById(transaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long transactionId) {
        // 1. 验证交易是否存在
        Transactions transaction = transactionsMapper.selectById(transactionId);
        if (transaction == null) {
            throw new ServiceException("交易不存在");
        }

        // 2. 验证交易状态
        if (transaction.getStatus() != 0) {
            throw new ServiceException("只能删除待支付的交易");
        }

        // 3. 删除交易记录
        transactionsMapper.deleteById(transactionId);
    }

    @Override
    @Transactional
    public void updateStatus(Long transactionId, Integer status) {
        // 1. 验证交易是否存在
        Transactions transaction = transactionsMapper.selectById(transactionId);
        if (transaction == null) {
            throw new ServiceException("交易不存在");
        }

        // 2. 更新状态
        transactionsMapper.updateStatus(transactionId, status);
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(Long transactionId, String thirdPartyTransactionId) {
        // 1. 验证交易是否存在
        Transactions transaction = transactionsMapper.selectById(transactionId);
        if (transaction == null) {
            throw new ServiceException("交易不存在");
        }

        // 2. 验证交易状态
        if (transaction.getStatus() != 0) {
            throw new ServiceException("交易状态不正确");
        }

        // 3. 更新交易状态和支付信息
        transaction.setStatus(1); // 支付成功
        transaction.setThirdPartyTransactionId(thirdPartyTransactionId);
        transaction.setPaymentTime(new Date());
        transaction.setUpdatedAt(new Date());
        transactionsMapper.updateById(transaction);

        // 4. 更新订单状态
        Orders order = ordersMapper.selectById(transaction.getOrderId());
        order.setStatus("pay"); // 已支付
        order.setUpdatedAt(new Date());
        ordersMapper.updateById(order);

        // 注意：直接购买不需要删除购物车项
    }

    @Override
    @Transactional
    public void handlePaymentFailure(Long transactionId, String reason) {
        // 1. 验证交易是否存在
        Transactions transaction = transactionsMapper.selectById(transactionId);
        if (transaction == null) {
            throw new ServiceException("交易不存在");
        }

        // 2. 验证交易状态
        if (transaction.getStatus() != 0) {
            throw new ServiceException("交易状态不正确");
        }

        // 3. 更新交易状态
        transaction.setStatus(2); // 支付失败
        transaction.setRemark(reason);
        transaction.setUpdatedAt(new Date());
        transactionsMapper.updateById(transaction);
    }

    @Override
    @Transactional
    public void handleRefund(Long transactionId, java.math.BigDecimal refundAmount, String reason) {
        // 1. 验证交易是否存在
        Transactions transaction = transactionsMapper.selectById(transactionId);
        if (transaction == null) {
            throw new ServiceException("交易不存在");
        }

        // 2. 验证交易状态
        if (transaction.getStatus() != 1) {
            throw new ServiceException("只能对支付成功的交易进行退款");
        }

        // 3. 验证退款金额
        if (refundAmount.compareTo(transaction.getAmount()) > 0) {
            throw new ServiceException("退款金额不能大于支付金额");
        }

        // 4. 更新退款信息
        Date now = new Date();
        transactionsMapper.updateRefund(transactionId, refundAmount, reason, now);

        // 5. 更新交易状态
        transaction.setStatus(3); // 已退款
        transaction.setUpdatedAt(now);
        transactionsMapper.updateById(transaction);

        // 6. 更新订单状态
        Orders order = ordersMapper.selectById(transaction.getOrderId());
        order.setStatus("cancelled"); // 已退款
        order.setUpdatedAt(now);
        ordersMapper.updateById(order);
    }

    @Override
    public Transactions getTransaction(Long transactionId) {
        Transactions transaction = transactionsMapper.selectById(transactionId);
        if (transaction == null) {
            throw new ServiceException("交易不存在");
        }
        return transaction;
    }

    @Override
    public Transactions getOrderTransaction(Long orderId) {
        Transactions transaction = transactionsMapper.selectByOrderId(orderId);
        if (transaction == null) {
            throw new ServiceException("交易不存在");
        }
        return transaction;
    }

    @Override
    public List<Transactions> getUserTransactions(Long userId) {
        return transactionsMapper.selectByUserId(userId);
    }

    @Override
    public List<Transactions> getTransactionsByStatus(Integer status) {
        return transactionsMapper.selectByStatus(status);
    }
}
