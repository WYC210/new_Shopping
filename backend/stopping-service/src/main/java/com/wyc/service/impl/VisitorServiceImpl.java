package com.wyc.service.impl;

import com.wyc.domain.dto.ProductDTO;
import com.wyc.mapper.ProductMapper;
import com.wyc.mapper.VisitorMapper;
import com.wyc.service.IVisitorService;
import com.wyc.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 访客服务实现类
 * 处理未登录用户的浏览记录，使用Redis存储
 *
 * @author wyc
 */
@Slf4j
@Service
public class VisitorServiceImpl implements IVisitorService {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private VisitorMapper visitorMapper;

    @Autowired
    private ProductMapper productMapper;

    private static final String VISITOR_BROWSING_KEY_PREFIX = "visitor_browsing:";
    private static final int BROWSING_HISTORY_EXPIRE_DAYS = 30; // 浏览记录保存30天
    private static final int MAX_BROWSING_HISTORY_SIZE = 50; // 最多保存50条浏览记录

    @Override
    public boolean recordVisitorBrowsing(String fingerprint, Long productId, String productName) {
        if (fingerprint == null || fingerprint.isEmpty() || productId == null) {
            return false;
        }

        try {
            // 使用与UserServiceImpl相同的键格式
            String key = "visitor:browsing:" + fingerprint;
            log.info("记录访客浏览商品: fingerprint={}, productId={}, productName={}, key={}",
                    fingerprint, productId, productName, key);

            // 构建浏览记录
            com.wyc.domain.vo.BrowsingRecordVO browsingRecord = new com.wyc.domain.vo.BrowsingRecordVO(
                    productId, productName, new Date());

            // 检查是否已经有此商品的浏览记录
            List<Object> existingRecords = redisCache.getCacheList(key);
            if (existingRecords != null && !existingRecords.isEmpty()) {
                log.debug("已存在浏览记录，记录数: {}", existingRecords.size());

                // 移除相同商品的旧记录
                Iterator<Object> iterator = existingRecords.iterator();
                while (iterator.hasNext()) {
                    Object record = iterator.next();
                    if (record instanceof com.wyc.domain.vo.BrowsingRecordVO) {
                        com.wyc.domain.vo.BrowsingRecordVO recordVO = (com.wyc.domain.vo.BrowsingRecordVO) record;
                        if (productId.equals(recordVO.getProductId())) {
                            iterator.remove();
                        }
                    } else if (record instanceof Map) {
                        Map<String, Object> recordMap = (Map<String, Object>) record;
                        if (productId.equals(recordMap.get("productId"))) {
                            iterator.remove();
                        }
                    }
                }

                // 添加新记录到开头
                existingRecords.add(0, browsingRecord);

                // 如果超过最大记录数，移除最旧的记录
                while (existingRecords.size() > MAX_BROWSING_HISTORY_SIZE) {
                    existingRecords.remove(existingRecords.size() - 1);
                }

                // 更新Redis
                redisCache.deleteObject(key); // 先删除旧记录
                redisCache.setCacheList(key, existingRecords);
            } else {
                log.debug("没有现有浏览记录，创建新列表");
                // 创建新的浏览记录列表
                List<Object> newRecords = new ArrayList<>();
                newRecords.add(browsingRecord);
                redisCache.setCacheList(key, newRecords);
            }

            // 设置过期时间
            redisCache.expire(key, BROWSING_HISTORY_EXPIRE_DAYS, TimeUnit.DAYS);

            // 尝试保存访客信息到数据库（如果不存在）
            visitorMapper.insertVisitorIfNotExists(fingerprint);

            log.info("成功记录访客浏览商品: fingerprint={}, productId={}", fingerprint, productId);
            return true;
        } catch (Exception e) {
            log.error("记录访客浏览商品失败: fingerprint={}, productId={}, error={}", fingerprint, productId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<Object> getVisitorBrowsingHistory(String fingerprint, int limit) {
        if (fingerprint == null || fingerprint.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String key = VISITOR_BROWSING_KEY_PREFIX + fingerprint;
            List<Object> browsingHistory = redisCache.getCacheList(key);

            if (browsingHistory == null || browsingHistory.isEmpty()) {
                return Collections.emptyList();
            }

            // 限制返回数量
            if (limit > 0 && browsingHistory.size() > limit) {
                return browsingHistory.subList(0, limit);
            }

            return browsingHistory;
        } catch (Exception e) {
            log.error("获取访客浏览历史失败: fingerprint={}, error={}", fingerprint, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean associateVisitorToUser(String fingerprint, Long userId) {
        if (fingerprint == null || fingerprint.isEmpty() || userId == null) {
            return false;
        }

        try {
            // 获取访客浏览记录
            String visitorKey = VISITOR_BROWSING_KEY_PREFIX + fingerprint;
            List<Object> visitorBrowsingHistory = redisCache.getCacheList(visitorKey);

            if (visitorBrowsingHistory == null || visitorBrowsingHistory.isEmpty()) {
                // 没有浏览记录，直接更新数据库中的关联关系
                visitorMapper.updateVisitorUserId(fingerprint, userId);
                return true;
            }

            // 将访客浏览记录转换为用户浏览记录并保存到数据库
            for (Object record : visitorBrowsingHistory) {
                if (record instanceof Map) {
                    Map<String, Object> recordMap = (Map<String, Object>) record;
                    Long productId = (Long) recordMap.get("productId");
                    String productName = (String) recordMap.get("productName");

                    if (productId != null) {
                        // 这里可以调用用户浏览记录服务保存到数据库
                        // 或者直接使用Mapper保存
                        visitorMapper.insertUserBrowsingRecord(userId, productId, productName);
                    }
                }
            }

            // 更新访客表中的用户ID
            visitorMapper.updateVisitorUserId(fingerprint, userId);

            // 清除Redis中的访客浏览记录
            redisCache.deleteObject(visitorKey);

            log.info("访客浏览记录关联到用户成功: fingerprint={}, userId={}", fingerprint, userId);
            return true;
        } catch (Exception e) {
            log.error("访客浏览记录关联到用户失败: fingerprint={}, userId={}, error={}",
                    fingerprint, userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean clearVisitorBrowsingHistory(String fingerprint) {
        if (fingerprint == null || fingerprint.isEmpty()) {
            return false;
        }

        try {
            String key = VISITOR_BROWSING_KEY_PREFIX + fingerprint;
            redisCache.deleteObject(key);
            log.debug("清除访客浏览记录成功: fingerprint={}", fingerprint);
            return true;
        } catch (Exception e) {
            log.error("清除访客浏览记录失败: fingerprint={}, error={}", fingerprint, e.getMessage(), e);
            return false;
        }
    }
}