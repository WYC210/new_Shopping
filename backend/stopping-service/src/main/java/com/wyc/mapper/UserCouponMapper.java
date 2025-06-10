package com.wyc.mapper;

import com.wyc.domain.vo.UserCouponVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserCouponMapper {

    /**
     * 获取用户优惠券列表
     *
     * @param userId 用户ID
     * @param status 优惠券状态
     * @return 优惠券列表
     */
    List<UserCouponVO> getUserCoupons(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 查询用户是否已领取过该优惠券
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @return 已领取的数量
     */
    int countUserCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

    /**
     * 添加用户优惠券
     *
     * @param userId     用户ID
     * @param couponId   优惠券ID
     * @param couponName 优惠券名称
     * @return 影响行数
     */
    int addUserCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId,
            @Param("couponName") String couponName);
}