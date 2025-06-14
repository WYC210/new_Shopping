package com.wyc.controller;

import com.wyc.domain.dto.ProductDTO;
import com.wyc.service.ISearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 搜索前端控制器
 * 提供基于数据库的搜索视图
 *
 * @author wyc
 */
@Controller
@RequestMapping("/search")
public class SearchViewController {

    @Autowired
    private ISearchService searchService;

    /**
     * 搜索页面
     */
    @GetMapping("")
    public String searchPage() {
        return "search/index";
    }

    /**
     * 搜索结果页
     * 使用数据库搜索功能
     */
    @GetMapping("/result")
    public String searchResult(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String brand,
            Model model) {

        List<ProductDTO> products;

        if (keyword != null && !keyword.isEmpty()) {
            // 如果有关键词，使用关键词搜索
            products = searchService.searchProducts(keyword);
            model.addAttribute("keyword", keyword);
        } else if (categoryId != null) {
            // 如果有分类ID，按分类搜索
            products = searchService.searchProductsByCategory(categoryId);
            model.addAttribute("categoryId", categoryId);
        } else if (minPrice != null && maxPrice != null) {
            // 如果有价格范围，按价格搜索
            products = searchService.searchProductsByPriceRange(minPrice, maxPrice);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
        } else if (brand != null && !brand.isEmpty()) {
            // 如果有品牌，使用高级搜索
            products = searchService.advancedSearch(null, null, null, null, brand);
            model.addAttribute("brand", brand);
        } else {
            // 如果都没有，使用高级搜索，可能同时包含多个条件
            products = searchService.advancedSearch(keyword, categoryId, minPrice, maxPrice, brand);
        }

        model.addAttribute("products", products);
        model.addAttribute("count", products.size());

        return "search/result";
    }

    /**
     * 高级搜索页面
     */
    @GetMapping("/advanced-search")
    public String advancedSearchPage() {
        return "search/advanced";
    }
}