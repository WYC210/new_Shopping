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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Map;

@Api(tags = "订单管理接口")
@RestController
@RequestMapping("/wyc/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @ApiOperation("创建订单")
    @PostMapping
    @CircuitBreaker(name = "createOrder", fallbackMethod = "createOrderFallback")
    public R<Long> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单创建信息", required = true) @RequestBody OrderCreateVO orderCreateVO) {
        Long userId = SecurityContext.getUserId();
        return R.ok(orderService.createOrder(userId, orderCreateVO));
    }

    @ApiOperation("获取订单详情")
    @GetMapping("/{orderId}")
    @CircuitBreaker(name = "getOrderDetail", fallbackMethod = "getOrderDetailFallback")
    public R<OrderDetailVO> getOrderDetail(
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
       // 熔断服务降级测试专用
        // try {
        //     Thread.sleep(2000); // 模拟耗时2秒
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt();
        // }
        return R.ok(orderService.getOrderDetail(orderId));
    }

    @ApiOperation("获取用户订单列表")
    @GetMapping("/user")
    @CircuitBreaker(name = "getUserOrders", fallbackMethod = "getUserOrdersFallback")
    public R<Map<String, Object>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单状态", required = false) @RequestParam(required = false) String status,
            @ApiParam(value = "页码", required = false) @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam(value = "每页条数", required = false) @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        Long userId = SecurityContext.getUserId();
        // 处理 status=all 的情况
        String queryStatus = (status != null && status.equals("all")) ? null : status;

        // 使用分页方法
        Map<String, Object> result = orderService.getUserOrdersWithPagination(userId, queryStatus, page, pageSize);
        return R.ok(result);
    }

    @ApiOperation("取消订单")
    @PostMapping("/{orderId}/cancel")
    @CircuitBreaker(name = "cancelOrder", fallbackMethod = "cancelOrderFallback")
    public R<Void> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        Long userId = SecurityContext.getUserId();
        orderService.cancelOrder(orderId, userId);
        return R.ok();
    }

    @ApiOperation("支付订单")
    @PostMapping("/{orderId}/pay")
    @CircuitBreaker(name = "payOrder", fallbackMethod = "payOrderFallback")
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
    @CircuitBreaker(name = "confirmReceipt", fallbackMethod = "confirmReceiptFallback")
    public R<Void> confirmReceipt(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        Long userId = SecurityContext.getUserId();
        orderService.confirmReceipt(orderId, userId);
        return R.ok();
    }

    @ApiOperation("删除订单")
    @DeleteMapping("/{orderId}")
    @CircuitBreaker(name = "deleteOrder", fallbackMethod = "deleteOrderFallback")
    public R<Void> deleteOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        Long userId = SecurityContext.getUserId();
        orderService.deleteOrder(orderId, userId);
        return R.ok();
    }

    @ApiOperation("直接购买商品")
    @PostMapping("/direct-purchase")
    @CircuitBreaker(name = "directPurchase", fallbackMethod = "directPurchaseFallback")
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

    // 创建订单 fallback
    public R<Long> createOrderFallback(UserDetails userDetails, OrderCreateVO orderCreateVO, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 获取订单详情 fallback
    public R<OrderDetailVO> getOrderDetailFallback(Long orderId, Throwable t) {
        System.out.println("触发了回调");
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 获取用户订单列表 fallback
    public R<Map<String, Object>> getUserOrdersFallback(UserDetails userDetails, String status, Integer page,
            Integer pageSize, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 取消订单 fallback
    public R<Void> cancelOrderFallback(UserDetails userDetails, Long orderId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 支付订单 fallback
    public R<Void> payOrderFallback(UserDetails userDetails, Long orderId, String paymentMethod, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 确认收货 fallback
    public R<Void> confirmReceiptFallback(UserDetails userDetails, Long orderId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 删除订单 fallback
    public R<Void> deleteOrderFallback(UserDetails userDetails, Long orderId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 直接购买商品 fallback
    public R<Long> directPurchaseFallback(UserDetails userDetails, DirectPurchaseVO directPurchaseVO, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }
}