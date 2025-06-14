package com.wyc.service;

import com.wyc.domain.dto.ProductDTO;

import java.util.List;

/**
 * 搜索服务接口
 *
 * @author wyc
 */
public interface ISearchService {

    /**
     * 根据关键词搜索商品
     *
     * @param keyword 关键词
     * @return 商品列表
     */
    List<ProductDTO> searchProducts(String keyword);

    /**
     * 根据分类ID搜索商品
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<ProductDTO> searchProductsByCategory(Long categoryId);

    /**
     * 根据价格区间搜索商品
     *
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 商品列表
     */
    List<ProductDTO> searchProductsByPriceRange(Double minPrice, Double maxPrice);

    /**
     * 高级搜索
     *
     * @param keyword    关键词
     * @param categoryId 分类ID
     * @param minPrice   最低价格
     * @param maxPrice   最高价格
     * @param brand      品牌
     * @return 商品列表
     */
    List<ProductDTO> advancedSearch(String keyword, Long categoryId, Double minPrice, Double maxPrice, String brand);
}