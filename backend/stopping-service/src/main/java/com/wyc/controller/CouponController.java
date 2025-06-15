package com.wyc.controller;

import com.wyc.domain.po.Coupons;
import com.wyc.domain.po.UserCoupons;
import com.wyc.domain.vo.UserCouponDetailVO;
import com.wyc.security.SecurityContext;
import com.wyc.service.ICouponService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Api(tags = "优惠券管理接口")
@RestController
@RequestMapping("/wyc/coupons")
public class CouponController {

    @Autowired
    private ICouponService couponService;

    @ApiOperation("创建优惠券")
    @PostMapping
    public R<Long> createCoupon(
            @ApiParam(value = "优惠券信息", required = true) @RequestBody Coupons coupon) {
        return R.ok(couponService.createCoupon(coupon));
    }

    @ApiOperation("获取优惠券详情")
    @GetMapping("/{couponId}")
    public R<Coupons> getCouponById(
            @ApiParam(value = "优惠券ID", required = true) @PathVariable Long couponId) {
        return R.ok(couponService.getCouponById(couponId));
    }

    @ApiOperation("获取可领取的优惠券列表")
    @GetMapping("/available")
    public R<List<Coupons>> getAvailableCoupons() {
        return R.ok(couponService.getAvailableCoupons());
    }

    @ApiOperation("领取优惠券")
    @PostMapping("/{couponId}/receive")
    public R<Long> receiveCoupon(
            @ApiParam(value = "优惠券ID", required = true) @PathVariable Long couponId) {
        Long userId = SecurityContext.getUserId();
        return R.ok(couponService.receiveCoupon(userId, couponId));
    }

    @ApiOperation("获取用户的优惠券列表")
    @GetMapping("/user")
    public R<List<UserCouponDetailVO>> getUserCoupons(
            @ApiParam(value = "优惠券状态", required = false) @RequestParam(required = false) String status) {
        Long userId = SecurityContext.getUserId();
        return R.ok(couponService.getUserCouponDetails(userId, status));
    }

    @ApiOperation("计算订单优惠金额")
    @GetMapping("/calculate")
    public R<Double> calculateDiscount(
            @ApiParam(value = "优惠券ID", required = true) @RequestParam Long couponId,
            @ApiParam(value = "订单金额", required = true) @RequestParam Double amount) {
        Long userId = SecurityContext.getUserId();
        return R.ok(couponService.calculateDiscount(userId, couponId, amount));
    }

    @ApiOperation("检查优惠券是否可用")
    @GetMapping("/check")
    public R<Boolean> isCouponAvailable(
            @ApiParam(value = "优惠券ID", required = true) @RequestParam Long couponId,
            @ApiParam(value = "订单金额", required = true) @RequestParam Double amount) {
        Long userId = SecurityContext.getUserId();
        return R.ok(couponService.isCouponAvailable(userId, couponId, amount));
    }

    // 创建优惠券 fallback
    public R<Long> createCouponFallback(Coupons coupon, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 获取优惠券详情 fallback
    public R<Coupons> getCouponByIdFallback(Long couponId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 获取可领取的优惠券列表 fallback
    public R<List<Coupons>> getAvailableCouponsFallback(Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 领取优惠券 fallback
    public R<Long> receiveCouponFallback(Long couponId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 获取用户的优惠券列表 fallback
    public R<List<UserCouponDetailVO>> getUserCouponsFallback(String status, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 计算订单优惠金额 fallback
    public R<Double> calculateDiscountFallback(Long couponId, Double amount, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 检查优惠券是否可用 fallback
    public R<Boolean> isCouponAvailableFallback(Long couponId, Double amount, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }
}