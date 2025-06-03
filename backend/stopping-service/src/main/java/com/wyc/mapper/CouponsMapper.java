package com.wyc.mapper;

import com.wyc.domain.po.Coupons;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Date;

@Mapper
public interface CouponsMapper {
    /**
     * 插入优惠券
     */
    int insert(Coupons coupon);

    /**
     * 根据ID查询优惠券
     */
    Coupons selectById(@Param("couponId") Long couponId);

    /**
     * 查询可领取的优惠券列表
     */
    List<Coupons> selectAvailable(@Param("now") Date now);

    /**
     * 更新优惠券已领取数量
     */
    int updateUsedCount(@Param("couponId") Long couponId, @Param("count") int count);

    /**
     * 更新优惠券信息
     */
    int updateById(Coupons coupon);

    /**
     * 删除优惠券
     */
    int deleteById(@Param("couponId") Long couponId);
}