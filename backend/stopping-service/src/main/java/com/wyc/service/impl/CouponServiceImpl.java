package com.wyc.service.impl;

import com.wyc.domain.po.Coupons;
import com.wyc.domain.po.UserCoupons;
import com.wyc.domain.vo.UserCouponDetailVO;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.CouponsMapper;
import com.wyc.mapper.UserCouponsMapper;
import com.wyc.service.ICouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements ICouponService {

    @Autowired
    private CouponsMapper couponsMapper;

    @Autowired
    private UserCouponsMapper userCouponsMapper;

    @Override
    @Transactional
    public Long createCoupon(Coupons coupon) {
        // 设置初始状态
        coupon.setUsedCount(0);
        coupon.setCreatedAt(new Date());
        coupon.setUpdatedAt(new Date());

        // 验证优惠券信息
        validateCoupon(coupon);

        // 保存优惠券
        couponsMapper.insert(coupon);
        return coupon.getCouponId();
    }

    @Override
    public Coupons getCouponById(Long couponId) {
        Coupons coupon = couponsMapper.selectById(couponId);
        if (coupon == null) {
            throw new ServiceException("优惠券不存在");
        }
        return coupon;
    }

    @Override
    public List<Coupons> getAvailableCoupons() {
        return couponsMapper.selectAvailable(new Date());
    }

    @Override
    @Transactional
    public Long receiveCoupon(Long userId, Long couponId) {
        // 1. 获取优惠券信息
        Coupons coupon = getCouponById(couponId);

        // 2. 验证优惠券是否可领取
        Date now = new Date();
        if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
            throw new ServiceException("优惠券不在有效期内");
        }

        // 3. 验证用户是否已达到领取限制
        Integer perLimit = coupon.getPerLimit();
        if (perLimit == null) {
            throw new ServiceException("每人限领数量未设置，请联系管理员！");
        }
        int receivedCount = userCouponsMapper.countByUserIdAndCouponId(userId, couponId);
        if (receivedCount >= perLimit) {
            throw new ServiceException("已达到领取限制");
        }

        // 4. 创建用户优惠券记录
        UserCoupons userCoupon = new UserCoupons();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setCouponName(coupon.getName());
        userCoupon.setStatus("unused");
        userCoupon.setCreatedAt(now);
        userCouponsMapper.insert(userCoupon);

        // 5. 更新优惠券已领取数量
        couponsMapper.updateUsedCount(couponId, 1);

        return userCoupon.getId();
    }

    @Override
    public List<UserCoupons> getUserCoupons(Long userId, String status) {
        // 转换状态参数为小写，以匹配数据库中的枚举值
        if (status != null) {
            status = status.toLowerCase();
        }
        return userCouponsMapper.selectByUserIdAndStatus(userId, status);
    }

    @Override
    @Transactional
    public void useCoupon(Long userId, Long couponId, Long orderId) {
        // 1. 获取用户优惠券记录
        List<UserCoupons> userCoupons = userCouponsMapper.selectByUserIdAndStatus(userId, "unused");
        UserCoupons userCoupon = userCoupons.stream()
                .filter(uc -> uc.getCouponId().equals(couponId))
                .findFirst()
                .orElseThrow(() -> new ServiceException("优惠券不存在或已使用"));

        // 2. 更新优惠券状态
        userCouponsMapper.updateStatus(userCoupon.getId(), "used");
    }

    @Override
    public double calculateDiscount(Long userId, Long couponId, double amount) {
        // 1. 验证优惠券是否可用
        if (!isCouponAvailable(userId, couponId, amount)) {
            return 0.0;
        }

        // 2. 获取优惠券信息
        Coupons coupon = getCouponById(couponId);

        // 3. 计算优惠金额
        if ("DISCOUNT".equals(coupon.getType())) {
            // 折扣券
            return amount * (1 - coupon.getValue().doubleValue() / 100);
        } else {
            // 满减券
            return coupon.getValue().doubleValue();
        }
    }

    @Override
    public boolean isCouponAvailable(Long userId, Long couponId, double amount) {
        // 1. 获取优惠券信息
        Coupons coupon = getCouponById(couponId);

        // 2. 验证优惠券是否在有效期内
        Date now = new Date();
        if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
            return false;
        }

        // 3. 验证订单金额是否满足使用门槛
        if (amount < coupon.getThreshold().doubleValue()) {
            return false;
        }

        // 4. 验证用户是否有可用的优惠券
        List<UserCoupons> userCoupons = userCouponsMapper.selectByUserIdAndStatus(userId, "unused");
        return userCoupons.stream()
                .anyMatch(uc -> uc.getCouponId().equals(couponId));
    }

    @Override
    public List<UserCouponDetailVO> getUserCouponDetails(Long userId, String status) {
        // 转换状态参数为小写，以匹配数据库中的枚举值
        if (status != null) {
            status = status.toLowerCase();
        }

        // 获取用户优惠券列表
        List<UserCoupons> userCoupons = userCouponsMapper.selectByUserIdAndStatus(userId, status);

        // 将用户优惠券转换为详情VO
        List<UserCouponDetailVO> result = new ArrayList<>(userCoupons.size());

        for (UserCoupons userCoupon : userCoupons) {
            UserCouponDetailVO detailVO = new UserCouponDetailVO();
            // 复制基本属性
            BeanUtils.copyProperties(userCoupon, detailVO);

            // 获取优惠券详情，添加value等信息
            Coupons coupon = couponsMapper.selectById(userCoupon.getCouponId());
            if (coupon != null) {
                detailVO.setType(coupon.getType());
                detailVO.setValue(coupon.getValue());
                detailVO.setThreshold(coupon.getThreshold());
            }

            result.add(detailVO);
        }

        return result;
    }

    /**
     * 验证优惠券信息
     */
    private void validateCoupon(Coupons coupon) {
        if (coupon.getStartTime() == null || coupon.getEndTime() == null) {
            throw new ServiceException("优惠券有效期不能为空");
        }
        if (coupon.getStartTime().after(coupon.getEndTime())) {
            throw new ServiceException("优惠券开始时间不能晚于结束时间");
        }
        if (coupon.getTotalCount() <= 0) {
            throw new ServiceException("优惠券发放数量必须大于0");
        }
        if (coupon.getPerLimit() <= 0) {
            throw new ServiceException("每人限领数量必须大于0");
        }
        if (coupon.getPerLimit() > coupon.getTotalCount()) {
            throw new ServiceException("每人限领数量不能大于总发放数量");
        }
        if (coupon.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("优惠金额或折扣率必须大于0");
        }
        if ("DISCOUNT".equals(coupon.getType()) && coupon.getValue().compareTo(new BigDecimal("100")) >= 0) {
            throw new ServiceException("折扣率不能大于或等于100%");
        }
    }
}
