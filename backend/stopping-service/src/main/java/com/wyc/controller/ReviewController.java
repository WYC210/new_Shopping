package com.wyc.controller;

import com.wyc.domain.po.Reviews;
import com.wyc.security.SecurityContext;
import com.wyc.service.IReviewService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Api(tags = "商品评价接口")
@RestController
@RequestMapping("/wyc/reviews")
public class ReviewController {

    @Autowired
    private IReviewService reviewService;

    @ApiOperation("创建评价")
    @PostMapping("/orders/{orderId}/items/{orderItemId}")
    public R<Long> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId,
            @ApiParam(value = "订单项ID", required = true) @PathVariable Long orderItemId,
            @ApiParam(value = "评价信息", required = true) @RequestBody Reviews review) {
        Long userId = SecurityContext.getUserId();
        return R.ok(reviewService.createReview(userId, orderId, orderItemId, review));
    }

    @ApiOperation("获取评价详情")
    @GetMapping("/{reviewId}")
    public R<Reviews> getReviewById(
            @ApiParam(value = "评价ID", required = true) @PathVariable Long reviewId) {
        return R.ok(reviewService.getReviewById(reviewId));
    }

    @ApiOperation("获取商品的评价列表")
    @GetMapping("/products/{productId}")
    public R<List<Reviews>> getProductReviews(
            @ApiParam(value = "商品ID", required = true) @PathVariable Long productId) {
        return R.ok(reviewService.getProductReviews(productId));
    }

    @ApiOperation("获取用户的评价列表")
    @GetMapping("/user")
    public R<List<Reviews>> getUserReviews(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return R.ok(reviewService.getUserReviews(userId));
    }

    @ApiOperation("获取订单的评价列表")
    @GetMapping("/orders/{orderId}")
    public R<List<Reviews>> getOrderReviews(
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        return R.ok(reviewService.getOrderReviews(orderId));
    }

    @ApiOperation("回复评价")
    @PostMapping("/{reviewId}/reply")
    public R<Void> replyReview(
            @ApiParam(value = "评价ID", required = true) @PathVariable Long reviewId,
            @ApiParam(value = "回复内容", required = true) @RequestParam String reply) {
        reviewService.replyReview(reviewId, reply);
        return R.ok();
    }

    @ApiOperation("点赞评价")
    @PostMapping("/{reviewId}/like")
    public R<Void> likeReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "评价ID", required = true) @PathVariable Long reviewId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        reviewService.likeReview(reviewId, userId);
        return R.ok();
    }

    @ApiOperation("取消点赞评价")
    @DeleteMapping("/{reviewId}/like")
    public R<Void> unlikeReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "评价ID", required = true) @PathVariable Long reviewId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        reviewService.unlikeReview(reviewId, userId);
        return R.ok();
    }

    @ApiOperation("删除评价")
    @DeleteMapping("/{reviewId}")
    public R<Void> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @ApiParam(value = "评价ID", required = true) @PathVariable Long reviewId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        reviewService.deleteReview(reviewId, userId);
        return R.ok();
    }

    @ApiOperation("获取商品的平均评分")
    @GetMapping("/products/{productId}/rating")
    public R<Double> getProductAverageRating(
            @ApiParam(value = "商品ID", required = true) @PathVariable Long productId) {
        return R.ok(reviewService.getProductAverageRating(productId));
    }

    @ApiOperation("获取商品的评价数量")
    @GetMapping("/products/{productId}/count")
    public R<Integer> getProductReviewCount(
            @ApiParam(value = "商品ID", required = true) @PathVariable Long productId) {
        return R.ok(reviewService.getProductReviewCount(productId));
    }

    // 创建评价 fallback
    public R<Long> createReviewFallback(UserDetails userDetails, Long orderId, Long orderItemId, Reviews review,
            Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 获取评价详情 fallback
    public R<Reviews> getReviewByIdFallback(Long reviewId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 获取商品的评价列表 fallback
    public R<List<Reviews>> getProductReviewsFallback(Long productId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 获取用户的评价列表 fallback
    public R<List<Reviews>> getUserReviewsFallback(UserDetails userDetails, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 获取订单的评价列表 fallback
    public R<List<Reviews>> getOrderReviewsFallback(Long orderId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 回复评价 fallback
    public R<Void> replyReviewFallback(Long reviewId, String reply, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 点赞评价 fallback
    public R<Void> likeReviewFallback(UserDetails userDetails, Long reviewId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 取消点赞评价 fallback
    public R<Void> unlikeReviewFallback(UserDetails userDetails, Long reviewId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 删除评价 fallback
    public R<Void> deleteReviewFallback(UserDetails userDetails, Long reviewId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 获取商品的平均评分 fallback
    public R<Double> getProductAverageRatingFallback(Long productId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    // 获取商品的评价数量 fallback
    public R<Integer> getProductReviewCountFallback(Long productId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }
}