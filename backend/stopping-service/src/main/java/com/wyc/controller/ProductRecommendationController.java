package com.wyc.controller;

import com.wyc.domain.po.ProductRecommendations;
import com.wyc.service.IProductRecommendationService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "商品推荐管理")
@RestController
@RequestMapping("/wyc/recommendations")
public class ProductRecommendationController {

    @Autowired
    private IProductRecommendationService recommendationService;


    @ApiOperation("添加商品推荐")
    @PostMapping
    @PreAuthorize("@ss.hasPermi('wyc:recommendation:add')")
    @CircuitBreaker(name = "addRecommendation", fallbackMethod = "addRecommendationFallback")
    public R<Long> addRecommendation(@RequestBody ProductRecommendations recommendation) {
        Long recommendationId = recommendationService.addRecommendation(recommendation);
        return R.ok(recommendationId);
    }

    @ApiOperation("更新商品推荐")
    @PutMapping
    @PreAuthorize("@ss.hasPermi('wyc:recommendation:edit')")
    @CircuitBreaker(name = "updateRecommendation", fallbackMethod = "updateRecommendationFallback")
    public R<Void> updateRecommendation(@RequestBody ProductRecommendations recommendation) {
        recommendationService.updateRecommendation(recommendation);
        return R.ok();
    }

    @ApiOperation("删除商品推荐")
    @DeleteMapping("/{recommendationId}")
    @PreAuthorize("@ss.hasPermi('wyc:recommendation:remove')")
    @CircuitBreaker(name = "deleteRecommendation", fallbackMethod = "deleteRecommendationFallback")
    public R<Void> deleteRecommendation(@PathVariable Long recommendationId) {
        recommendationService.deleteRecommendation(recommendationId);
        return R.ok();
    }

    @ApiOperation("更新推荐状态")
    @PutMapping("/{recommendationId}/status/{status}")
    @PreAuthorize("@ss.hasPermi('wyc:recommendation:edit')")
    @CircuitBreaker(name = "updateStatus", fallbackMethod = "updateStatusFallback")
    public R<Void> updateStatus(
            @ApiParam("推荐ID") @PathVariable Long recommendationId,
            @ApiParam("状态") @PathVariable Integer status) {
        recommendationService.updateStatus(recommendationId, status);
        return R.ok();
    }

    @ApiOperation("获取推荐详情")
    @GetMapping("/{recommendationId}")
    @CircuitBreaker(name = "getRecommendation", fallbackMethod = "getRecommendationFallback")
    public R<ProductRecommendations> getRecommendation(@PathVariable Long recommendationId) {
        ProductRecommendations recommendation = recommendationService.getRecommendation(recommendationId);
        return R.ok(recommendation);
    }

    @ApiOperation("获取首页推荐")
    @GetMapping("/home")
    @CircuitBreaker(name = "getHomeRecommendations", fallbackMethod = "getHomeRecommendationsFallback")
    public R<List<ProductRecommendations>> getHomeRecommendations() {
        List<ProductRecommendations> recommendations = recommendationService.getHomeRecommendations();
        return R.ok(recommendations);
    }

    @ApiOperation("获取分类推荐")
    @GetMapping("/category/{categoryId}")
    @CircuitBreaker(name = "getCategoryRecommendations", fallbackMethod = "getCategoryRecommendationsFallback")
    public R<List<ProductRecommendations>> getCategoryRecommendations(@PathVariable Long categoryId) {
        List<ProductRecommendations> recommendations = recommendationService.getCategoryRecommendations(categoryId);
        return R.ok(recommendations);
    }

    @ApiOperation("获取相关商品推荐")
    @GetMapping("/related/{productId}")
    @CircuitBreaker(name = "getRelatedProducts", fallbackMethod = "getRelatedProductsFallback")
    public R<List<ProductRecommendations>> getRelatedProducts(@PathVariable Long productId) {
        List<ProductRecommendations> recommendations = recommendationService.getRelatedProducts(productId);
        return R.ok(recommendations);
    }

    @ApiOperation("获取个性化推荐")
    @GetMapping("/personalized/{userId}")
    @CircuitBreaker(name = "getPersonalizedRecommendations", fallbackMethod = "getPersonalizedRecommendationsFallback")
    public R<List<ProductRecommendations>> getPersonalizedRecommendations(@PathVariable Long userId) {
        List<ProductRecommendations> recommendations = recommendationService.getPersonalizedRecommendations(userId);
        return R.ok(recommendations);
    }

    @ApiOperation("获取购物车推荐")
    @GetMapping("/cart")
    @CircuitBreaker(name = "getCartRecommendations", fallbackMethod = "getCartRecommendationsFallback")
    public R<List<ProductRecommendations>> getCartRecommendations() {
        try {
            // 直接返回空列表，不进行商品推荐
            return R.ok(new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            return R.ok(new ArrayList<>());
        }
    }

    public R<Long> addRecommendationFallback(ProductRecommendations recommendation, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<Void> updateRecommendationFallback(ProductRecommendations recommendation, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<Void> deleteRecommendationFallback(Long recommendationId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<Void> updateStatusFallback(Long recommendationId, Integer status, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<ProductRecommendations> getRecommendationFallback(Long recommendationId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<List<ProductRecommendations>> getHomeRecommendationsFallback(Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<List<ProductRecommendations>> getCategoryRecommendationsFallback(Long categoryId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<List<ProductRecommendations>> getRelatedProductsFallback(Long productId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<List<ProductRecommendations>> getPersonalizedRecommendationsFallback(Long userId, Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<List<ProductRecommendations>> getCartRecommendationsFallback(Throwable t) {
        return R.error(503, "服务暂时不可用，请稍后重试");
    }
}