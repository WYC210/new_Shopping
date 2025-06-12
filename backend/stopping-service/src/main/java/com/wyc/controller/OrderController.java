package com.wyc.controller;

import com.wyc.domain.vo.OrderCreateVO;
import com.wyc.domain.vo.OrderDetailVO;
import com.wyc.domain.vo.DirectPurchaseVO;
import com.wyc.security.SecurityContext;
import com.wyc.service.IOrderService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "订单管理接口")
@RestController
@RequestMapping("/wyc/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @ApiOperation("创建订单")
    @PostMapping
    public R<Long> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单创建信息", required = true) @RequestBody OrderCreateVO orderCreateVO) {
        Long userId = SecurityContext.getUserId();
        return R.ok(orderService.createOrder(userId, orderCreateVO));
    }

    @ApiOperation("获取订单详情")
    @GetMapping("/{orderId}")
    public R<OrderDetailVO> getOrderDetail(
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        return R.ok(orderService.getOrderDetail(orderId));
    }

    @ApiOperation("获取用户订单列表")
    @GetMapping("/user")
    public R<List<OrderDetailVO>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单状态", required = false) @RequestParam(required = false) String status) {
        Long userId = SecurityContext.getUserId();
        // 处理 status=all 的情况
        String queryStatus = (status != null && status.equals("all")) ? null : status;
        return R.ok(orderService.getUserOrders(userId, queryStatus));
    }

    @ApiOperation("取消订单")
    @PostMapping("/{orderId}/cancel")
    public R<Void> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        Long userId = SecurityContext.getUserId();
        orderService.cancelOrder(orderId, userId);
        return R.ok();
    }

    @ApiOperation("支付订单")
    @PostMapping("/{orderId}/pay")
    public R<Void> payOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId,
            @ApiParam(value = "支付方式", required = true) @RequestParam String paymentMethod) {
        Long userId = SecurityContext.getUserId();
        orderService.payOrder(orderId, userId, paymentMethod);
        return R.ok();
    }

    @ApiOperation("确认收货")
    @PostMapping("/{orderId}/confirm")
    public R<Void> confirmReceipt(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        Long userId = SecurityContext.getUserId();
        orderService.confirmReceipt(orderId, userId);
        return R.ok();
    }

    @ApiOperation("删除订单")
    @DeleteMapping("/{orderId}")
    public R<Void> deleteOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        Long userId = SecurityContext.getUserId();
        orderService.deleteOrder(orderId, userId);
        return R.ok();
    }

    @ApiOperation("直接购买商品")
    @PostMapping("/direct-purchase")
    public R<Long> directPurchase(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "直接购买信息", required = true) @RequestBody DirectPurchaseVO directPurchaseVO) {
        Long userId = SecurityContext.getUserId();
        // 转换为订单创建对象
        OrderCreateVO orderCreateVO = new OrderCreateVO();
        orderCreateVO.setItems(directPurchaseVO.getItems());
        orderCreateVO.setAddressId(directPurchaseVO.getAddressId());
        orderCreateVO.setRemark(directPurchaseVO.getRemark());

        // 创建订单
        Long orderId = orderService.createOrder(userId, orderCreateVO);

        // 如果提供了支付方式，直接支付
        if (directPurchaseVO.getPaymentMethod() != null && !directPurchaseVO.getPaymentMethod().isEmpty()) {
            orderService.payOrder(orderId, userId, directPurchaseVO.getPaymentMethod());
        }

        return R.ok(orderId);
    }
}