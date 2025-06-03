package com.wyc.mapper;

import com.wyc.domain.po.UserCoupons;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserCouponsMapper {
    /**
     * 插入用户优惠券
     */
    int insert(UserCoupons userCoupon);

    /**
     * 根据ID查询用户优惠券
     */
    UserCoupons selectById(@Param("id") Long id);

    /**
     * 查询用户的优惠券列表
     */
    List<UserCoupons> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 查询用户是否已领取过该优惠券
     */
    int countByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);

    /**
     * 更新用户优惠券状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 删除用户优惠券
     */
    int deleteById(@Param("id") Long id);
}