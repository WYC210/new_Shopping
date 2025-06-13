package com.wyc.controller;

import com.wyc.domain.dto.ProductDTO;
import com.wyc.domain.dto.ProductDetailDTO;
import com.wyc.service.IProductService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品相关接口")
@RestController
@RequestMapping("/wyc/products")
public class ProductController {

    @Autowired
    private IProductService productService;

    @ApiOperation("获取热门商品")
    @GetMapping("/hot")
    @CircuitBreaker(name = "getHotProducts", fallbackMethod = "getHotProductsFallback")
    @RateLimiter(name = "getHotProductsRateLimiter")
    @Bulkhead(name = "getHotProductsBulkhead")
    public R<List<ProductDTO>> getHotProducts() {
        return R.ok(productService.getHotProducts());
    }

    @ApiOperation("获取新上架商品")
    @GetMapping("/new")
    @CircuitBreaker(name = "getNewProducts", fallbackMethod = "getNewProductsFallback")
    @RateLimiter(name = "getNewProductsRateLimiter")
    @Bulkhead(name = "getNewProductsBulkhead")
    public R<List<ProductDTO>> getNewProducts() {
        return R.ok(productService.getNewProducts());
    }

    @ApiOperation("获取商品详情")
    @GetMapping("/{productId}")
    @CircuitBreaker(name = "getProductDetail", fallbackMethod = "getProductDetailFallback")
    @RateLimiter(name = "getProductDetailRateLimiter")
    @Bulkhead(name = "getProductDetailBulkhead")
    public R<ProductDetailDTO> getProductDetail(
            @ApiParam(value = "商品ID", required = true) @PathVariable Long productId) {
        // 熔断服务降级测试专用        
        // try {
        //     Thread.sleep(2000); // 模拟耗时2秒
        // } catch (InterruptedException e) {
        //     Thread.currentThread().interrupt();
        // }
        return R.ok(productService.getProductDetail(productId));
    }

    @ApiOperation("获取分类商品")
    @GetMapping("/category/{categoryId}")
    @CircuitBreaker(name = "getProductsByCategory", fallbackMethod = "getProductsByCategoryFallback")
    @RateLimiter(name = "getProductsByCategoryRateLimiter")
    @Bulkhead(name = "getProductsByCategoryBulkhead")
    public R<List<ProductDTO>> getProductsByCategory(
            @ApiParam(value = "分类ID", required = true) @PathVariable Long categoryId) {
        return R.ok(productService.getProductsByCategory(categoryId));
    }

    @ApiOperation("搜索商品")
    @GetMapping("/search")
    @CircuitBreaker(name = "searchProducts", fallbackMethod = "searchProductsFallback")
    @RateLimiter(name = "searchProductsRateLimiter")
    @Bulkhead(name = "searchProductsBulkhead")
    public R<List<ProductDTO>> searchProducts(
            @ApiParam(value = "搜索关键词", required = true) @RequestParam String keyword) {
        return R.ok(productService.searchProducts(keyword));
    }

    public R<List<ProductDTO>> getHotProductsFallback(Throwable t) {
        return R.error("获取热门商品失败");
    }

    public R<List<ProductDTO>> getNewProductsFallback(Throwable t) {
        return R.error("获取新上架商品失败");
    }

    public R<ProductDetailDTO> getProductDetailFallback(Long productId, Throwable t) {
        System.out.println("【降级触发】getProductDetailFallback 被调用，商品ID: " + productId + "，异常: " + t.getMessage());
        return R.error("服务降级，获取商品详情失败，请稍后重试");
    }

    public R<List<ProductDTO>> getProductsByCategoryFallback(Long categoryId, Throwable t) {
        return R.error("获取分类商品失败");
    }

    public R<List<ProductDTO>> searchProductsFallback(String keyword, Throwable t) {
        return R.error("搜索商品失败");
    }
}
