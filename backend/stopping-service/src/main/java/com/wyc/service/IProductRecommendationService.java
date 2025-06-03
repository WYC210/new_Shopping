package com.wyc.service;

import com.wyc.domain.po.ProductRecommendations;
import java.util.List;

public interface IProductRecommendationService {
    /**
     * 添加商品推荐
     *
     * @param recommendation 推荐信息
     * @return 推荐ID
     */
    Long addRecommendation(ProductRecommendations recommendation);

    /**
     * 更新商品推荐
     *
     * @param recommendation 推荐信息
     */
    void updateRecommendation(ProductRecommendations recommendation);

    /**
     * 删除商品推荐
     *
     * @param recommendationId 推荐ID
     */
    void deleteRecommendation(Long recommendationId);

    /**
     * 更新推荐状态
     *
     * @param recommendationId 推荐ID
     * @param status           状态
     */
    void updateStatus(Long recommendationId, Integer status);

    /**
     * 获取推荐详情
     *
     * @param recommendationId 推荐ID
     * @return 推荐信息
     */
    ProductRecommendations getRecommendation(Long recommendationId);

    /**
     * 获取首页推荐商品
     *
     * @return 推荐列表
     */
    List<ProductRecommendations> getHomeRecommendations();

    /**
     * 获取分类推荐商品
     *
     * @param categoryId 分类ID
     * @return 推荐列表
     */
    List<ProductRecommendations> getCategoryRecommendations(Long categoryId);

    /**
     * 获取相关商品推荐
     *
     * @param productId 商品ID
     * @return 推荐列表
     */
    List<ProductRecommendations> getRelatedProducts(Long productId);

    /**
     * 获取个性化推荐商品
     *
     * @param userId 用户ID
     * @return 推荐列表
     */
    List<ProductRecommendations> getPersonalizedRecommendations(Long userId);
}