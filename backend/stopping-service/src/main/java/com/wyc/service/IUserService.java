package com.wyc.service;

import com.wyc.domain.dto.LoginDTO;
import com.wyc.domain.vo.*;
import com.wyc.utils.R;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface IUserService {
    /**
     * 用户注册
     */
    void register(UserRegisterVO registerVO);

    /**
     * 用户登录
     */
    String login(LoginDTO loginDTO);

    /**
     * 用户退出
     */
    void logout(HttpServletRequest request);

    /**
     * 获取用户详情
     */
    R getUserDetails(Long userId);

    /**
     * 更新用户余额
     *
     * @param userId 用户ID
     * @param amount 变动金额（正数增加，负数减少）
     */
    void updateBalance(Long userId, Double amount);

    /**
     * 更新用户密码
     *
     * @param userId           用户ID
     * @param passwordUpdateVO 密码更新VO
     */
    void updatePassword(Long userId, PasswordUpdateVO passwordUpdateVO);

    /**
     * 更新用户资料
     *
     * @param userId          用户ID
     * @param profileUpdateVO 用户资料更新VO
     */
    void updateProfile(Long userId, UserProfileUpdateVO profileUpdateVO);

    /**
     * 获取用户优惠券
     *
     * @param userId 用户ID
     * @param status 优惠券状态
     * @return 用户优惠券列表
     */
    List<UserCouponVO> getUserCoupons(Long userId, String status);

    /**
     * 获取用户浏览历史
     *
     * @param userId 用户ID
     * @return 浏览历史列表
     */
    List<BrowsingRecordVO> getBrowsingHistory(Long userId);

    /**
     * 记录浏览历史
     *
     * @param fingerprint 浏览器指纹
     * @param productId   商品ID
     * @param productName 商品名称
     */
    void recordBrowsing(String fingerprint, Long productId, String productName);
}
