package com.wyc.service.impl;

import com.wyc.domain.po.ProductRecommendations;
import com.wyc.domain.po.Products;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.ProductRecommendationsMapper;
import com.wyc.mapper.ProductsMapper;
import com.wyc.service.IProductRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ProductRecommendationServiceImpl implements IProductRecommendationService {

    @Autowired
    private ProductRecommendationsMapper recommendationsMapper;

    @Autowired
    private ProductsMapper productsMapper;

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
        return recommendationsMapper.selectByType(1);
    }

    @Override
    public List<ProductRecommendations> getCategoryRecommendations(Long categoryId) {
        return recommendationsMapper.selectByType(2);
    }

    @Override
    public List<ProductRecommendations> getRelatedProducts(Long productId) {
        // 1. 获取商品信息
        Products product = productsMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }

        // 2. 获取相关商品推荐
        return recommendationsMapper.selectRelatedProducts(productId, product.getCategoryId());
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