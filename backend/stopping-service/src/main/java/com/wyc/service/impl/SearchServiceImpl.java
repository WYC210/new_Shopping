package com.wyc.service.impl;

import com.wyc.domain.dto.ProductDTO;
import com.wyc.mapper.ProductMapper;
import com.wyc.service.ISearchService;
import com.wyc.utils.RedisCache;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 搜索服务实现类
 * 使用数据库模糊查询实现搜索功能
 *
 * @author wyc
 */
@Slf4j
@Service
public class SearchServiceImpl implements ISearchService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisCache redisCache;

    private static final String SEARCH_CACHE_KEY_PREFIX = "search_";
    private static final int SEARCH_CACHE_EXPIRATION = 60 * 5; // 5分钟

    @Override
    @CircuitBreaker(name = "searchService", fallbackMethod = "searchFallback")
    public List<ProductDTO> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 记录开始时间，用于性能监控
        StopWatch stopWatch = new StopWatch("SearchProducts");
        stopWatch.start("GetFromCache");

        // 尝试从缓存获取
        String cacheKey = SEARCH_CACHE_KEY_PREFIX + "keyword_" + keyword;
        List<ProductDTO> cachedResult = redisCache.getCacheObject(cacheKey);
        stopWatch.stop();

        if (cachedResult != null) {
            log.debug("从缓存中获取搜索结果: keyword={}, resultSize={}", keyword, cachedResult.size());
            return cachedResult;
        }

        // 从数据库搜索
        stopWatch.start("SearchFromDatabase");
        List<ProductDTO> result = productMapper.searchProducts(keyword);
        stopWatch.stop();

        // 缓存搜索结果
        if (!result.isEmpty()) {
            stopWatch.start("CacheResults");
            redisCache.setCacheObject(cacheKey, result, SEARCH_CACHE_EXPIRATION, TimeUnit.SECONDS);
            stopWatch.stop();
        }

        log.debug("搜索耗时统计: {}", stopWatch.prettyPrint());
        return result;
    }

    /**
     * 当searchProducts方法失败时的回退方法
     */
    public List<ProductDTO> searchFallback(String keyword, Exception e) {
        log.error("搜索服务熔断，返回空结果。关键词: {}, 错误: {}", keyword, e.getMessage());
        return Collections.emptyList();
    }

    @Override
    public List<ProductDTO> searchProductsByCategory(Long categoryId) {
        if (categoryId == null) {
            return Collections.emptyList();
        }

        String cacheKey = SEARCH_CACHE_KEY_PREFIX + "category_" + categoryId;
        List<ProductDTO> cachedResult = redisCache.getCacheObject(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        List<ProductDTO> result = productMapper.getProductsByCategory(categoryId);
        if (!result.isEmpty()) {
            redisCache.setCacheObject(cacheKey, result, SEARCH_CACHE_EXPIRATION, TimeUnit.SECONDS);
        }
        return result;
    }

    @Override
    public List<ProductDTO> searchProductsByPriceRange(Double minPrice, Double maxPrice) {
        if (minPrice == null || maxPrice == null || minPrice > maxPrice) {
            return Collections.emptyList();
        }

        String cacheKey = SEARCH_CACHE_KEY_PREFIX + "price_" + minPrice + "_" + maxPrice;
        List<ProductDTO> cachedResult = redisCache.getCacheObject(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 使用数据库查询价格范围内的商品
        List<ProductDTO> result = productMapper.searchProductsByPriceRange(minPrice, maxPrice);
        if (!result.isEmpty()) {
            redisCache.setCacheObject(cacheKey, result, SEARCH_CACHE_EXPIRATION, TimeUnit.SECONDS);
        }
        return result;
    }

    @Override
    public List<ProductDTO> advancedSearch(String keyword, Long categoryId, Double minPrice, Double maxPrice,
            String brand) {
        // 构建缓存键，考虑到组合条件太多，这里只对特定组合进行缓存
        StringBuilder cacheKeyBuilder = new StringBuilder(SEARCH_CACHE_KEY_PREFIX + "advanced_");
        if (keyword != null && !keyword.trim().isEmpty()) {
            cacheKeyBuilder.append("kw_").append(keyword).append("_");
        }
        if (categoryId != null) {
            cacheKeyBuilder.append("cat_").append(categoryId).append("_");
        }
        if (minPrice != null && maxPrice != null) {
            cacheKeyBuilder.append("price_").append(minPrice).append("_").append(maxPrice).append("_");
        }
        if (brand != null && !brand.trim().isEmpty()) {
            cacheKeyBuilder.append("brand_").append(brand);
        }

        String cacheKey = cacheKeyBuilder.toString();

        // 如果缓存键有效（不是只有前缀），则尝试从缓存获取
        if (!cacheKey.equals(SEARCH_CACHE_KEY_PREFIX + "advanced_")) {
            List<ProductDTO> cachedResult = redisCache.getCacheObject(cacheKey);
            if (cachedResult != null) {
                log.debug("从缓存中获取高级搜索结果: cacheKey={}, resultSize={}", cacheKey, cachedResult.size());
                return cachedResult;
            }
        }

        // 使用数据库进行高级搜索
        List<ProductDTO> result = productMapper.advancedSearchProducts(keyword, categoryId, minPrice, maxPrice, brand);

        // 如果结果不为空且缓存键有效，则缓存结果
        if (!result.isEmpty() && !cacheKey.equals(SEARCH_CACHE_KEY_PREFIX + "advanced_")) {
            redisCache.setCacheObject(cacheKey, result, SEARCH_CACHE_EXPIRATION, TimeUnit.SECONDS);
            log.debug("缓存高级搜索结果: cacheKey={}, resultSize={}", cacheKey, result.size());
        }

        return result != null ? result : Collections.emptyList();
    }
}