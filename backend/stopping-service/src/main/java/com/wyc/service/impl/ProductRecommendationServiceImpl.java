package com.wyc.service.impl;

import com.wyc.domain.po.ProductRecommendations;
import com.wyc.domain.po.Products;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.ProductRecommendationsMapper;
import com.wyc.mapper.ProductsMapper;
import com.wyc.service.IProductRecommendationService;
import com.wyc.utils.RedisCache;
import com.wyc.utils.BloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class ProductRecommendationServiceImpl implements IProductRecommendationService {

    @Autowired
    private ProductRecommendationsMapper recommendationsMapper;

    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private RedisCache redisCache;

    private static final String HOME_RECOMMEND_KEY = "home_recommend";
    private static final String CATEGORY_RECOMMEND_KEY_PREFIX = "category_recommend_";
    private static final String RELATED_PRODUCTS_KEY_PREFIX = "related_products_";
    private static final int CACHE_BASE_TIMEOUT = 60 * 10;
    private static final int CACHE_RANDOM_BOUND = 60 * 5;
    private static final List<ProductRecommendations> EMPTY_RECOMMEND_LIST = Collections.emptyList();
    private static final BloomFilter categoryBloomFilter = new BloomFilter();
    private static final BloomFilter productBloomFilter = new BloomFilter();
    private static final Random random = new Random();

    @Override
    @Transactional
    public Long addRecommendation(ProductRecommendations recommendation) {
        // 1. 验证商品是否存在
        Products product = productsMapper.selectById(recommendation.getProductId());
        if (product == null) {
            throw new ServiceException("商品不存在");
        }

        // 2. 设置创建和更新时间
        recommendation.setCreatedAt(new Date());
        recommendation.setUpdatedAt(new Date());

        // 3. 保存推荐
        recommendationsMapper.insert(recommendation);
        return recommendation.getRecommendationId();
    }

    @Override
    @Transactional
    public void updateRecommendation(ProductRecommendations recommendation) {
        // 1. 验证推荐是否存在
        ProductRecommendations existingRecommendation = recommendationsMapper
                .selectById(recommendation.getRecommendationId());
        if (existingRecommendation == null) {
            throw new ServiceException("推荐不存在");
        }

        // 2. 验证商品是否存在
        Products product = productsMapper.selectById(recommendation.getProductId());
        if (product == null) {
            throw new ServiceException("商品不存在");
        }

        // 3. 设置更新时间
        recommendation.setUpdatedAt(new Date());

        // 4. 更新推荐
        recommendationsMapper.updateById(recommendation);
    }

    @Override
    @Transactional
    public void deleteRecommendation(Long recommendationId) {
        // 1. 验证推荐是否存在
        ProductRecommendations recommendation = recommendationsMapper.selectById(recommendationId);
        if (recommendation == null) {
            throw new ServiceException("推荐不存在");
        }

        // 2. 删除推荐
        recommendationsMapper.deleteById(recommendationId);
    }

    @Override
    @Transactional
    public void updateStatus(Long recommendationId, Integer status) {
        // 1. 验证推荐是否存在
        ProductRecommendations recommendation = recommendationsMapper.selectById(recommendationId);
        if (recommendation == null) {
            throw new ServiceException("推荐不存在");
        }

        // 2. 更新状态
        recommendationsMapper.updateStatus(recommendationId, status);
    }

    @Override
    public ProductRecommendations getRecommendation(Long recommendationId) {
        ProductRecommendations recommendation = recommendationsMapper.selectById(recommendationId);
        if (recommendation == null) {
            throw new ServiceException("推荐不存在");
        }
        return recommendation;
    }

    @Override
    public List<ProductRecommendations> getHomeRecommendations() {
        String cacheKey = HOME_RECOMMEND_KEY;
        List<ProductRecommendations> cached = redisCache.getCacheList(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        List<ProductRecommendations> list = recommendationsMapper.selectByType(1);
        if (list == null || list.isEmpty()) {
            redisCache.setCacheList(cacheKey, EMPTY_RECOMMEND_LIST);
            redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
            return EMPTY_RECOMMEND_LIST;
        }
        redisCache.setCacheList(cacheKey, list);
        redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
        return list;
    }

    @Override
    public List<ProductRecommendations> getCategoryRecommendations(Long categoryId) {
        if (!categoryBloomFilter.mightContain(categoryId.toString())) {
            return EMPTY_RECOMMEND_LIST;
        }
        String cacheKey = CATEGORY_RECOMMEND_KEY_PREFIX + categoryId;
        List<ProductRecommendations> cached = redisCache.getCacheList(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        List<ProductRecommendations> list = recommendationsMapper.selectByType(2);
        if (list == null || list.isEmpty()) {
            redisCache.setCacheList(cacheKey, EMPTY_RECOMMEND_LIST);
            redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
            return EMPTY_RECOMMEND_LIST;
        }
        redisCache.setCacheList(cacheKey, list);
        redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
        return list;
    }

    @Override
    public List<ProductRecommendations> getRelatedProducts(Long productId) {
        if (!productBloomFilter.mightContain(productId.toString())) {
            return EMPTY_RECOMMEND_LIST;
        }
        String cacheKey = RELATED_PRODUCTS_KEY_PREFIX + productId;
        List<ProductRecommendations> cached = redisCache.getCacheList(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        Products product = productsMapper.selectById(productId);
        if (product == null) {
            redisCache.setCacheList(cacheKey, EMPTY_RECOMMEND_LIST);
            redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
            return EMPTY_RECOMMEND_LIST;
        }
        List<ProductRecommendations> list = recommendationsMapper.selectRelatedProducts(productId,
                product.getCategoryId());
        if (list == null || list.isEmpty()) {
            redisCache.setCacheList(cacheKey, EMPTY_RECOMMEND_LIST);
            redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
            return EMPTY_RECOMMEND_LIST;
        }
        redisCache.setCacheList(cacheKey, list);
        redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
        return list;
    }

    @Override
    public List<ProductRecommendations> getPersonalizedRecommendations(Long userId) {
        // TODO: 实现基于用户行为的个性化推荐算法
        // 1. 获取用户历史行为数据（浏览、收藏、购买等）
        // 2. 分析用户兴趣偏好
        // 3. 基于协同过滤或内容推荐算法生成推荐列表
        // 4. 返回推荐结果
        return recommendationsMapper.selectByType(4);
    }
}