package com.wyc.service.impl;

import com.wyc.domain.po.Orders;
import com.wyc.domain.po.Transactions;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.OrdersMapper;
import com.wyc.mapper.TransactionsMapper;
import com.wyc.service.IMessageService;
import com.wyc.service.ITransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionServiceImpl implements ITransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Autowired
    private TransactionsMapper transactionsMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private IMessageService messageService;

    @Override
    @Transactional
    public Long createTransaction(Transactions transaction) {
        logger.info("开始创建交易: orderId={}", transaction.getOrderId());

        // 1. 验证订单是否存在
        Orders order = ordersMapper.selectById(transaction.getOrderId());
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        logger.debug("订单状态：{}", order.getStatus());

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

        logger.info("交易创建成功: transactionId={}", transaction.getTransactionId());
        return transaction.getTransactionId();
    }

    @Override
    @Transactional
    public void updateTransaction(Transactions transaction) {
        logger.info("开始更新交易: transactionId={}", transaction.getTransactionId());

        // 1. 验证交易是否存在
        Transactions existingTransaction = transactionsMapper.selectById(transaction.getTransactionId());
        if (existingTransaction == null) {
            throw new ServiceException("交易不存在");
        }

        // 2. 设置更新时间
        transaction.setUpdatedAt(new Date());

        // 3. 更新交易信息
        transactionsMapper.updateById(transaction);

        logger.info("交易更新成功: transactionId={}", transaction.getTransactionId());
    }

    @Override
    @Transactional
    public void deleteTransaction(Long transactionId) {
        logger.info("开始删除交易: transactionId={}", transactionId);

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

        logger.info("交易删除成功: transactionId={}", transactionId);
    }

    @Override
    @Transactional
    public void updateStatus(Long transactionId, Integer status) {
        logger.info("开始更新交易状态: transactionId={}, status={}", transactionId, status);

        // 1. 验证交易是否存在
        Transactions transaction = transactionsMapper.selectById(transactionId);
        if (transaction == null) {
            throw new ServiceException("交易不存在");
        }

        // 2. 更新状态
        transactionsMapper.updateStatus(transactionId, status);

        logger.info("交易状态更新成功: transactionId={}, status={}", transactionId, status);
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(Long transactionId, String thirdPartyTransactionId) {
        logger.info("开始处理支付成功: transactionId={}, thirdPartyTransactionId={}", transactionId, thirdPartyTransactionId);

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

        // 5. 发送支付成功消息
        sendPaymentSuccessMessage(transaction, order);

        logger.info("支付成功处理完成: transactionId={}, orderId={}", transactionId, transaction.getOrderId());
    }

    @Override
    @Transactional
    public void handlePaymentFailure(Long transactionId, String reason) {
        logger.info("开始处理支付失败: transactionId={}, reason={}", transactionId, reason);

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

        // 4. 发送支付失败通知
        sendPaymentFailureNotification(transaction, reason);

        logger.info("支付失败处理完成: transactionId={}", transactionId);
    }

    @Override
    @Transactional
    public void handleRefund(Long transactionId, java.math.BigDecimal refundAmount, String reason) {
        logger.info("开始处理退款: transactionId={}, refundAmount={}, reason={}",
                transactionId, refundAmount, reason);

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

        // 7. 发送退款成功通知
        sendRefundSuccessNotification(transaction, order, refundAmount, reason);

        logger.info("退款处理完成: transactionId={}, orderId={}",
                transactionId, transaction.getOrderId());
    }

    @Override
    public Transactions getTransaction(Long transactionId) {
        logger.debug("获取交易信息: transactionId={}", transactionId);

        Transactions transaction = transactionsMapper.selectById(transactionId);
        if (transaction == null) {
            throw new ServiceException("交易不存在");
        }
        return transaction;
    }

    @Override
    public Transactions getOrderTransaction(Long orderId) {
        logger.debug("获取订单交易信息: orderId={}", orderId);

        Transactions transaction = transactionsMapper.selectByOrderId(orderId);
        if (transaction == null) {
            throw new ServiceException("交易不存在");
        }
        return transaction;
    }

    @Override
    public List<Transactions> getUserTransactions(Long userId) {
        logger.debug("获取用户交易列表: userId={}", userId);
        return transactionsMapper.selectByUserId(userId);
    }

    @Override
    public List<Transactions> getTransactionsByStatus(Integer status) {
        logger.debug("获取指定状态的交易列表: status={}", status);
        return transactionsMapper.selectByStatus(status);
    }

    /**
     * 发送支付成功消息
     */
    private void sendPaymentSuccessMessage(Transactions transaction, Orders order) {
        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transaction.getTransactionId());
        data.put("orderId", transaction.getOrderId());
        data.put("userId", order.getUserId());
        data.put("amount", transaction.getAmount());
        data.put("thirdPartyTransactionId", transaction.getThirdPartyTransactionId());
        data.put("paymentTime", transaction.getPaymentTime());

        messageService.sendPaymentSuccessMessage(
                transaction.getOrderId(),
                transaction.getTransactionId(),
                data);

        // 发送通知给用户
        messageService.sendNotificationMessage(
                order.getUserId(),
                "支付成功",
                "您的订单 #" + order.getOrderId() + " 支付成功，交易金额：" + transaction.getAmount() + "元。",
                data);
    }

    /**
     * 发送支付失败通知
     */
    private void sendPaymentFailureNotification(Transactions transaction, String reason) {
        Orders order = ordersMapper.selectById(transaction.getOrderId());
        if (order == null) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transaction.getTransactionId());
        data.put("orderId", transaction.getOrderId());
        data.put("userId", order.getUserId());
        data.put("reason", reason);

        messageService.sendNotificationMessage(
                order.getUserId(),
                "支付失败",
                "您的订单 #" + order.getOrderId() + " 支付失败，原因：" + reason + "。",
                data);
    }

    /**
     * 发送退款成功通知
     */
    private void sendRefundSuccessNotification(Transactions transaction, Orders order,
            java.math.BigDecimal refundAmount, String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("transactionId", transaction.getTransactionId());
        data.put("orderId", transaction.getOrderId());
        data.put("userId", order.getUserId());
        data.put("refundAmount", refundAmount);
        data.put("reason", reason);

        messageService.sendNotificationMessage(
                order.getUserId(),
                "退款成功",
                "您的订单 #" + order.getOrderId() + " 已退款，退款金额：" + refundAmount + "元。",
                data);
    }
}
