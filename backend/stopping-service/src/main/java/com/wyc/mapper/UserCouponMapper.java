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
}