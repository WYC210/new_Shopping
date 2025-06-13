package com.wyc.service.impl;

import com.wyc.service.ISignInService;
import com.wyc.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

@Service
public class SignInServiceImpl implements ISignInService {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取当前日期在当月的偏移量（0-30）
     */
    private int getDayOfMonth() {
        return LocalDate.now().getDayOfMonth() - 1;
    }

    /**
     * 获取当前年月，格式：yyyyMM
     */
    private String getYearMonth() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "" + String.format("%02d", now.getMonthValue());
    }

    /**
     * 获取用户当月签到key
     */
    private String getMonthSignKey(Long userId) {
        return "user:sign:" + userId + ":" + getYearMonth();
    }

    /**
     * 获取用户连续签到key
     */
    private String getContinuousSignKey(Long userId) {
        return "user:sign:continuous:" + userId;
    }

    /**
     * 获取签到排行榜key
     */
    private String getSignRankKey() {
        return "user:sign:rank:" + getYearMonth();
    }

    @Override
    public int signIn(Long userId) {
        int dayOfMonth = getDayOfMonth();
        String key = getMonthSignKey(userId);

        // 检查是否已签到
        if (Boolean.TRUE.equals(redisCache.getBit(key, dayOfMonth))) {
            return getContinuousSignCount(userId);
        }

        // 签到
        redisCache.setBit(key, dayOfMonth, true);

        // 设置过期时间，保留两个月
        LocalDateTime expireTime = LocalDate.now().plusMonths(2).atStartOfDay();
        long expireSeconds = (expireTime.atZone(ZoneId.systemDefault()).toEpochSecond() -
                System.currentTimeMillis() / 1000);
        redisCache.expire(key, (int) expireSeconds, java.util.concurrent.TimeUnit.SECONDS);

        // 更新连续签到天数
        int continuousCount = 1;

        // 检查昨天是否签到
        if (dayOfMonth > 0) {
            // 检查昨天是否签到
            if (Boolean.TRUE.equals(redisCache.getBit(key, dayOfMonth - 1))) {
                // 获取之前的连续签到天数
                Integer previousCount = redisCache.getCacheObject(getContinuousSignKey(userId));
                continuousCount = (previousCount != null ? previousCount : 0) + 1;
            }
        } else {
            // 当月第一天，检查上个月最后一天是否签到
            LocalDate lastMonth = LocalDate.now().minusMonths(1);
            String lastMonthKey = "user:sign:" + userId + ":" +
                    lastMonth.getYear() + String.format("%02d", lastMonth.getMonthValue());
            int lastDayOfLastMonth = YearMonth.of(lastMonth.getYear(), lastMonth.getMonth()).lengthOfMonth() - 1;

            if (Boolean.TRUE.equals(redisCache.getBit(lastMonthKey, lastDayOfLastMonth))) {
                // 获取之前的连续签到天数
                Integer previousCount = redisCache.getCacheObject(getContinuousSignKey(userId));
                continuousCount = (previousCount != null ? previousCount : 0) + 1;
            }
        }

        // 更新连续签到天数
        redisCache.setCacheObject(getContinuousSignKey(userId), continuousCount);

        // 更新签到排行榜
        redisTemplate.opsForZSet().incrementScore(getSignRankKey(), userId, 1);

        return continuousCount;
    }

    @Override
    public boolean isSignedToday(Long userId) {
        String key = getMonthSignKey(userId);
        int dayOfMonth = getDayOfMonth();
        return Boolean.TRUE.equals(redisCache.getBit(key, dayOfMonth));
    }

    @Override
    public Map<Integer, Boolean> getMonthSignRecord(Long userId) {
        String key = getMonthSignKey(userId);
        int daysInMonth = YearMonth.now().lengthOfMonth();

        Map<Integer, Boolean> result = new HashMap<>();
        for (int i = 0; i < daysInMonth; i++) {
            result.put(i + 1, Boolean.TRUE.equals(redisCache.getBit(key, i)));
        }

        return result;
    }

    @Override
    public int getMonthSignCount(Long userId) {
        String key = getMonthSignKey(userId);
        Long count = redisCache.bitCount(key);
        return count != null ? count.intValue() : 0;
    }

    @Override
    public int getContinuousSignCount(Long userId) {
        Integer count = redisCache.getCacheObject(getContinuousSignKey(userId));
        return count != null ? count : 0;
    }

    @Override
    public List<Map<String, Object>> getSignInRank(int limit) {
        String rankKey = getSignRankKey();
        Set<Object> topUsers = redisTemplate.opsForZSet().reverseRange(rankKey, 0, limit - 1);

        if (topUsers == null || topUsers.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;

        for (Object userId : topUsers) {
            Double score = redisTemplate.opsForZSet().score(rankKey, userId);
            if (score != null) {
                Map<String, Object> userRank = new HashMap<>();
                userRank.put("userId", userId);
                userRank.put("rank", rank++);
                userRank.put("signCount", score.intValue());
                result.add(userRank);
            }
        }

        return result;
    }
}