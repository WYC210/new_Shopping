package com.wyc.service.impl;

import com.wyc.domain.po.Reviews;
import com.wyc.domain.po.Orderitems;
import com.wyc.domain.po.Orders;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.ReviewsMapper;
import com.wyc.mapper.OrderitemsMapper;
import com.wyc.mapper.OrdersMapper;
import com.wyc.service.IReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ReviewServiceImpl implements IReviewService {

    @Autowired
    private ReviewsMapper reviewsMapper;

    @Autowired
    private OrderitemsMapper orderitemsMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Override
    @Transactional
    public Long createReview(Long userId, Long orderId, Long orderItemId, Reviews review) {
        // 1. 验证订单和订单项
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new ServiceException("订单不存在或无权评价");
        }
        if (!"COMPLETED".equals(order.getStatus())) {
            throw new ServiceException("订单未完成，不能评价");
        }

        Orderitems orderItem = orderitemsMapper.selectById(orderItemId);
        if (orderItem == null || !orderItem.getOrderId().equals(orderId)) {
            throw new ServiceException("订单项不存在");
        }

        // 2. 验证是否已评价
        List<Reviews> existingReviews = reviewsMapper.selectByOrderId(orderId);
        if (existingReviews.stream().anyMatch(r -> r.getOrderItemId().equals(orderItemId))) {
            throw new ServiceException("该商品已评价");
        }

        // 3. 设置评价信息
        review.setUserId(userId);
        review.setOrderId(orderId);
        review.setOrderItemId(orderItemId);
        review.setProductId(orderItem.getProductId());
        review.setLikeCount(0);
        review.setCreatedAt(new Date());
        review.setUpdatedAt(new Date());

        // 4. 验证评分
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new ServiceException("评分必须在1-5之间");
        }

        // 5. 保存评价
        reviewsMapper.insert(review);
        return review.getReviewId();
    }

    @Override
    public Reviews getReviewById(Long reviewId) {
        Reviews review = reviewsMapper.selectById(reviewId);
        if (review == null) {
            throw new ServiceException("评价不存在");
        }
        return review;
    }

    @Override
    public List<Reviews> getProductReviews(Long productId) {
        return reviewsMapper.selectByProductId(productId);
    }

    @Override
    public List<Reviews> getUserReviews(Long userId) {
        return reviewsMapper.selectByUserId(userId);
    }

    @Override
    public List<Reviews> getOrderReviews(Long orderId) {
        return reviewsMapper.selectByOrderId(orderId);
    }

    @Override
    @Transactional
    public void replyReview(Long reviewId, String reply) {
        // 1. 获取评价信息
        Reviews review = getReviewById(reviewId);

        // 2. 更新回复信息
        review.setReply(reply);
        review.setReplyTime(new Date());
        review.setUpdatedAt(new Date());
        reviewsMapper.updateById(review);
    }

    @Override
    @Transactional
    public void likeReview(Long reviewId, Long userId) {
        // 1. 获取评价信息
        Reviews review = getReviewById(reviewId);

        // 2. 更新点赞数
        reviewsMapper.updateLikeCount(reviewId, 1);
    }

    @Override
    @Transactional
    public void unlikeReview(Long reviewId, Long userId) {
        // 1. 获取评价信息
        Reviews review = getReviewById(reviewId);

        // 2. 更新点赞数
        reviewsMapper.updateLikeCount(reviewId, -1);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        // 1. 获取评价信息
        Reviews review = getReviewById(reviewId);

        // 2. 验证权限
        if (!review.getUserId().equals(userId)) {
            throw new ServiceException("无权删除此评价");
        }

        // 3. 删除评价
        reviewsMapper.deleteById(reviewId);
    }

    @Override
    public Double getProductAverageRating(Long productId) {
        return reviewsMapper.getAverageRating(productId);
    }

    @Override
    public Integer getProductReviewCount(Long productId) {
        return reviewsMapper.getReviewCount(productId);
    }
}