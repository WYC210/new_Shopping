package com.wyc.controller;

import com.wyc.domain.dto.ProductDTO;
import com.wyc.domain.vo.Result;
import com.wyc.service.ISearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 * 所有搜索接口无需登录即可访问
 *
 * @author wyc
 */
@Slf4j
@RestController
@RequestMapping("/search")
@Api(tags = "搜索接口")
public class SearchController {

    @Autowired
    private ISearchService searchService;

    /**
     * 搜索商品
     * 无需登录即可访问
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    @GetMapping("/products")
    @ApiOperation("搜索商品")
    public Result<List<ProductDTO>> searchProducts(
            @ApiParam("搜索关键词") @RequestParam String keyword) {
        log.info("搜索商品: keyword={}", keyword);
        List<ProductDTO> products = searchService.searchProducts(keyword);
        return Result.success(products);
    }

    /**
     * 根据分类搜索商品
     * 无需登录即可访问
     *
     * @param categoryId 分类ID
     * @return 搜索结果
     */
    @GetMapping("/category/{categoryId}")
    @ApiOperation("根据分类搜索商品")
    public Result<List<ProductDTO>> searchProductsByCategory(
            @ApiParam("分类ID") @PathVariable Long categoryId) {
        log.info("分类搜索商品: categoryId={}", categoryId);
        List<ProductDTO> products = searchService.searchProductsByCategory(categoryId);
        return Result.success(products);
    }

    /**
     * 根据价格区间搜索商品
     * 无需登录即可访问
     *
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 搜索结果
     */
    @GetMapping("/price")
    @ApiOperation("根据价格区间搜索商品")
    public Result<List<ProductDTO>> searchProductsByPriceRange(
            @ApiParam("最低价格") @RequestParam Double minPrice,
            @ApiParam("最高价格") @RequestParam Double maxPrice) {
        log.info("价格区间搜索商品: minPrice={}, maxPrice={}", minPrice, maxPrice);
        List<ProductDTO> products = searchService.searchProductsByPriceRange(minPrice, maxPrice);
        return Result.success(products);
    }

    /**
     * 高级搜索
     * 无需登录即可访问
     *
     * @param keyword    关键词
     * @param categoryId 分类ID
     * @param minPrice   最低价格
     * @param maxPrice   最高价格
     * @param brand      品牌
     * @return 搜索结果
     */
    @GetMapping("/advanced")
    @ApiOperation("高级搜索")
    public Result<List<ProductDTO>> advancedSearch(
            @ApiParam("搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam("最低价格") @RequestParam(required = false) Double minPrice,
            @ApiParam("最高价格") @RequestParam(required = false) Double maxPrice,
            @ApiParam("品牌") @RequestParam(required = false) String brand) {
        log.info("高级搜索: keyword={}, categoryId={}, priceRange=[{}, {}], brand={}",
                keyword, categoryId, minPrice, maxPrice, brand);
        List<ProductDTO> products = searchService.advancedSearch(keyword, categoryId, minPrice, maxPrice, brand);
        return Result.success(products);
    }
}
