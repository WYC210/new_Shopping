package com.wyc.service;

import com.wyc.domain.po.Coupons;
import com.wyc.domain.po.UserCoupons;
import java.util.List;

public interface ICouponService {
    /**
     * 创建优惠券
     *
     * @param coupon 优惠券信息
     * @return 优惠券ID
     */
    Long createCoupon(Coupons coupon);

    /**
     * 获取优惠券详情
     *
     * @param couponId 优惠券ID
     * @return 优惠券信息
     */
    Coupons getCouponById(Long couponId);

    /**
     * 获取所有可领取的优惠券列表
     *
     * @return 优惠券列表
     */
    List<Coupons> getAvailableCoupons();

    /**
     * 用户领取优惠券
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券ID
     */
    Long receiveCoupon(Long userId, Long couponId);

    /**
     * 获取用户的优惠券列表
     *
     * @param userId 用户ID
     * @param status 优惠券状态（可选）
     * @return 用户优惠券列表
     */
    List<UserCoupons> getUserCoupons(Long userId, String status);

    /**
     * 使用优惠券
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @param orderId  订单ID
     */
    void useCoupon(Long userId, Long couponId, Long orderId);

    /**
     * 计算订单优惠金额
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @param amount   订单金额
     * @return 优惠金额
     */
    double calculateDiscount(Long userId, Long couponId, double amount);

    /**
     * 检查优惠券是否可用
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @param amount   订单金额
     * @return 是否可用
     */
    boolean isCouponAvailable(Long userId, Long couponId, double amount);
}