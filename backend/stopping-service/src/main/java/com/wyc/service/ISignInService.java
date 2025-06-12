package com.wyc.service;

import java.util.List;
import java.util.Map;

/**
 * 签到服务接口
 */
public interface ISignInService {

    /**
     * 用户签到
     * 
     * @param userId 用户ID
     * @return 连续签到天数
     */
    int signIn(Long userId);

    /**
     * 检查用户今天是否已签到
     * 
     * @param userId 用户ID
     * @return 是否已签到
     */
    boolean isSignedToday(Long userId);

    /**
     * 获取用户当月签到记录
     * 
     * @param userId 用户ID
     * @return 签到记录，key为日期（1-31），value为是否签到
     */
    Map<Integer, Boolean> getMonthSignRecord(Long userId);

    /**
     * 获取用户当月签到天数
     * 
     * @param userId 用户ID
     * @return 签到天数
     */
    int getMonthSignCount(Long userId);

    /**
     * 获取用户连续签到天数
     * 
     * @param userId 用户ID
     * @return 连续签到天数
     */
    int getContinuousSignCount(Long userId);

    /**
     * 获取用户签到排名
     * 
     * @param limit 获取前几名
     * @return 排名列表，包含用户ID和签到天数
     */
    List<Map<String, Object>> getSignInRank(int limit);
}