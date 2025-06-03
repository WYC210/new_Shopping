package com.wyc.service;

import com.wyc.domain.po.Reviews;
import java.util.List;

public interface IReviewService {
    /**
     * 创建评价
     *
     * @param userId      用户ID
     * @param orderId     订单ID
     * @param orderItemId 订单项ID
     * @param review      评价信息
     * @return 评价ID
     */
    Long createReview(Long userId, Long orderId, Long orderItemId, Reviews review);

    /**
     * 获取评价详情
     *
     * @param reviewId 评价ID
     * @return 评价信息
     */
    Reviews getReviewById(Long reviewId);

    /**
     * 获取商品的评价列表
     *
     * @param productId 商品ID
     * @return 评价列表
     */
    List<Reviews> getProductReviews(Long productId);

    /**
     * 获取用户的评价列表
     *
     * @param userId 用户ID
     * @return 评价列表
     */
    List<Reviews> getUserReviews(Long userId);

    /**
     * 获取订单的评价列表
     *
     * @param orderId 订单ID
     * @return 评价列表
     */
    List<Reviews> getOrderReviews(Long orderId);

    /**
     * 回复评价
     *
     * @param reviewId 评价ID
     * @param reply    回复内容
     */
    void replyReview(Long reviewId, String reply);

    /**
     * 点赞评价
     *
     * @param reviewId 评价ID
     * @param userId   用户ID
     */
    void likeReview(Long reviewId, Long userId);

    /**
     * 取消点赞评价
     *
     * @param reviewId 评价ID
     * @param userId   用户ID
     */
    void unlikeReview(Long reviewId, Long userId);

    /**
     * 删除评价
     *
     * @param reviewId 评价ID
     * @param userId   用户ID
     */
    void deleteReview(Long reviewId, Long userId);

    /**
     * 获取商品的平均评分
     *
     * @param productId 商品ID
     * @return 平均评分
     */
    Double getProductAverageRating(Long productId);

    /**
     * 获取商品的评价数量
     *
     * @param productId 商品ID
     * @return 评价数量
     */
    Integer getProductReviewCount(Long productId);
}