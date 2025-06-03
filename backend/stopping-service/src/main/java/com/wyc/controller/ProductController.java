package com.wyc.controller;


import com.wyc.domain.dto.ProductDTO;
import com.wyc.domain.dto.ProductDetailDTO;
import com.wyc.service.IProductService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
    public R<List<ProductDTO>> getHotProducts() {
        return R.ok(productService.getHotProducts());
    }

    @ApiOperation("获取新上架商品")
    @GetMapping("/new")
    public R<List<ProductDTO>> getNewProducts() {
        return R.ok(productService.getNewProducts());
    }

    @ApiOperation("获取商品详情")
    @GetMapping("/{productId}")
    public R<ProductDetailDTO> getProductDetail(
            @ApiParam(value = "商品ID", required = true) @PathVariable Long productId) {
        return R.ok(productService.getProductDetail(productId));
    }

    @ApiOperation("获取分类商品")
    @GetMapping("/category/{categoryId}")
    public R<List<ProductDTO>> getProductsByCategory(
            @ApiParam(value = "分类ID", required = true) @PathVariable Long categoryId) {
        return R.ok(productService.getProductsByCategory(categoryId));
    }

    @ApiOperation("搜索商品")
    @GetMapping("/search")
    public R<List<ProductDTO>> searchProducts(
            @ApiParam(value = "搜索关键词", required = true) @RequestParam String keyword) {
        return R.ok(productService.searchProducts(keyword));
    }
}
