package com.wyc.controller;

import com.wyc.domain.po.Transactions;
import com.wyc.service.ITransactionService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "支付交易管理")
@RestController
@RequestMapping("/wyc/transactions")
public class TransactionController {

    @Autowired
    private ITransactionService transactionService;

    @ApiOperation("创建交易")
    @PostMapping
    @PreAuthorize("@ss.hasPermi('wyc:transaction:add')")
    public R<Long> createTransaction(@RequestBody Transactions transaction) {
        Long transactionId = transactionService.createTransaction(transaction);
        return R.ok(transactionId);
    }

    @ApiOperation("更新交易")
    @PutMapping
    @PreAuthorize("@ss.hasPermi('wyc:transaction:edit')")
    public R<Void> updateTransaction(@RequestBody Transactions transaction) {
        transactionService.updateTransaction(transaction);
        return R.ok();
    }

    @ApiOperation("删除交易")
    @DeleteMapping("/{transactionId}")
    @PreAuthorize("@ss.hasPermi('wyc:transaction:remove')")
    public R<Void> deleteTransaction(@PathVariable Long transactionId) {
        transactionService.deleteTransaction(transactionId);
        return R.ok();
    }

    @ApiOperation("更新交易状态")
    @PutMapping("/{transactionId}/status/{status}")
    @PreAuthorize("@ss.hasPermi('wyc:transaction:edit')")
    public R<Void> updateStatus(
            @ApiParam("交易ID") @PathVariable Long transactionId,
            @ApiParam("状态") @PathVariable Integer status) {
        transactionService.updateStatus(transactionId, status);
        return R.ok();
    }

    @ApiOperation("处理支付成功")
    @PostMapping("/{transactionId}/success")
    @PreAuthorize("@ss.hasPermi('wyc:transaction:edit')")
    public R<Void> handlePaymentSuccess(
            @ApiParam("交易ID") @PathVariable Long transactionId,
            @ApiParam("第三方支付交易号") @RequestParam String thirdPartyTransactionId) {
        transactionService.handlePaymentSuccess(transactionId, thirdPartyTransactionId);
        return R.ok();
    }

    @ApiOperation("处理支付失败")
    @PostMapping("/{transactionId}/failure")
    @PreAuthorize("@ss.hasPermi('wyc:transaction:edit')")
    public R<Void> handlePaymentFailure(
            @ApiParam("交易ID") @PathVariable Long transactionId,
            @ApiParam("失败原因") @RequestParam String reason) {
        transactionService.handlePaymentFailure(transactionId, reason);
        return R.ok();
    }

    @ApiOperation("处理退款")
    @PostMapping("/{transactionId}/refund")
    @PreAuthorize("@ss.hasPermi('wyc:transaction:edit')")
    public R<Void> handleRefund(
            @ApiParam("交易ID") @PathVariable Long transactionId,
            @ApiParam("退款金额") @RequestParam java.math.BigDecimal refundAmount,
            @ApiParam("退款原因") @RequestParam String reason) {
        transactionService.handleRefund(transactionId, refundAmount, reason);
        return R.ok();
    }

    @ApiOperation("获取交易详情")
    @GetMapping("/{transactionId}")
    public R<Transactions> getTransaction(@PathVariable Long transactionId) {
        Transactions transaction = transactionService.getTransaction(transactionId);
        return R.ok(transaction);
    }

    @ApiOperation("获取订单交易")
    @GetMapping("/order/{orderId}")
    public R<Transactions> getOrderTransaction(@PathVariable Long orderId) {
        Transactions transaction = transactionService.getOrderTransaction(orderId);
        return R.ok(transaction);
    }

    @ApiOperation("获取用户交易列表")
    @GetMapping("/user/{userId}")
    public R<List<Transactions>> getUserTransactions(@PathVariable Long userId) {
        List<Transactions> transactions = transactionService.getUserTransactions(userId);
        return R.ok(transactions);
    }

    @ApiOperation("获取状态交易列表")
    @GetMapping("/status/{status}")
    public R<List<Transactions>> getTransactionsByStatus(@PathVariable Integer status) {
        List<Transactions> transactions = transactionService.getTransactionsByStatus(status);
        return R.ok(transactions);
    }
}
