package com.wyc.controller;

import com.wyc.domain.dto.ProductDTO;
import com.wyc.domain.dto.ProductDetailDTO;
import com.wyc.service.IProductService;
import com.wyc.service.IVisitorService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Api(tags = "商品相关接口")
@RestController
@RequestMapping("/wyc/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private IProductService productService;

    @Autowired
    private IVisitorService visitorService;

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
            @ApiParam(value = "商品ID", required = true) @PathVariable Long productId,
            HttpServletRequest request) {

        // 从请求头获取浏览器指纹
        String fingerprint = request.getHeader("X-Fingerprint");
        logger.info("获取商品详情: productId={}, fingerprint={}", productId, fingerprint);

        // 获取商品详情
        ProductDetailDTO productDetail = productService.getProductDetail(productId);

        // 如果有浏览器指纹，记录浏览历史
        if (fingerprint != null && !fingerprint.isEmpty()) {
            logger.info("记录浏览历史: productId={}, productName={}, fingerprint={}",
                    productId, productDetail.getName(), fingerprint);
            try {
                visitorService.recordVisitorBrowsing(fingerprint, productId, productDetail.getName());
                logger.info("成功记录浏览历史");
            } catch (Exception e) {
                logger.error("记录浏览历史失败: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("未获取到浏览器指纹，无法记录浏览历史");
        }

        return R.ok(productDetail);
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

    @ApiOperation("测试浏览记录功能")
    @GetMapping("/test-browsing/{productId}")
    public R<?> testBrowsing(
            @ApiParam(value = "商品ID", required = true) @PathVariable Long productId,
            HttpServletRequest request) {

        // 从请求头获取浏览器指纹
        String fingerprint = request.getHeader("X-Fingerprint");
        logger.info("测试浏览记录功能: productId={}, fingerprint={}", productId, fingerprint);

        if (fingerprint == null || fingerprint.isEmpty()) {
            return R.error("X-Fingerprint请求头不能为空");
        }

        // 获取商品详情
        ProductDetailDTO productDetail = productService.getProductDetail(productId);

        // 记录浏览历史
        try {
            boolean result = visitorService.recordVisitorBrowsing(fingerprint, productId, productDetail.getName());
            logger.info("记录浏览历史结果: {}", result);

            // 获取浏览记录
            List<Object> records = visitorService.getVisitorBrowsingHistory(fingerprint, 10);
            logger.info("获取到浏览记录数: {}", records.size());

            Map<String, Object> response = new HashMap<>();
            response.put("result", result);
            response.put("records", records);
            return R.ok(response);
        } catch (Exception e) {
            logger.error("测试浏览记录失败: {}", e.getMessage(), e);
            return R.error("测试失败: " + e.getMessage());
        }
    }

    public R<List<ProductDTO>> getHotProductsFallback(Throwable t) {
        return R.error("获取热门商品失败");
    }

    public R<List<ProductDTO>> getNewProductsFallback(Throwable t) {
        return R.error("获取新上架商品失败");
    }

    public R<ProductDetailDTO> getProductDetailFallback(Long productId, HttpServletRequest request, Throwable t) {
        logger.error("获取商品详情失败: productId={}, error={}", productId, t.getMessage(), t);
        return R.error(503, "服务暂时不可用，请稍后重试");
    }

    public R<List<ProductDTO>> getProductsByCategoryFallback(Long categoryId, Throwable t) {
        return R.error("获取分类商品失败");
    }

    public R<List<ProductDTO>> searchProductsFallback(String keyword, Throwable t) {
        return R.error("搜索商品失败");
    }
}
