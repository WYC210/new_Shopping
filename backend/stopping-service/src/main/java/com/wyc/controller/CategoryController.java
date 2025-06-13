package com.wyc.controller;

import com.wyc.domain.po.Categories;
import com.wyc.domain.vo.CategoryVO;
import com.wyc.service.ICategoryService;
import com.wyc.utils.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;

@Api(tags = "商品分类接口")
@RestController
@RequestMapping("/wyc/categories")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @ApiOperation("获取所有分类")
    @GetMapping
    @CircuitBreaker(name = "getAllCategories", fallbackMethod = "getAllCategoriesFallback")
    public R<List<CategoryVO>> getAllCategories() {
        return R.ok(categoryService.getAllCategories());
    }

    @ApiOperation("获取所有可见分类")
    @GetMapping("/visible")
    @CircuitBreaker(name = "getVisibleCategories", fallbackMethod = "getVisibleCategoriesFallback")
    public R<List<CategoryVO>> getVisibleCategories() {
        return R.ok(categoryService.getVisibleCategories());
    }

    @ApiOperation("获取分类详情")
    @GetMapping("/{categoryId}")
    @CircuitBreaker(name = "getCategoryById", fallbackMethod = "getCategoryByIdFallback")
    public R<CategoryVO> getCategoryById(
            @ApiParam(value = "分类ID", required = true) @PathVariable Long categoryId) {
        return R.ok(categoryService.getCategoryById(categoryId));
    }

    @ApiOperation("获取子分类")
    @GetMapping("/{parentId}/children")
    @CircuitBreaker(name = "getChildCategories", fallbackMethod = "getChildCategoriesFallback")
    public R<List<CategoryVO>> getChildCategories(
            @ApiParam(value = "父分类ID", required = true) @PathVariable Long parentId) {
        return R.ok(categoryService.getChildCategories(parentId));
    }

    @ApiOperation("新增分类")
    @PostMapping
    @CircuitBreaker(name = "addCategory", fallbackMethod = "addCategoryFallback")
    public R<Void> addCategory(
            @ApiParam(value = "分类信息", required = true) @RequestBody Categories category) {
        categoryService.addCategory(category);
        return R.ok();
    }

    @ApiOperation("修改分类")
    @PutMapping("/{categoryId}")
    @CircuitBreaker(name = "updateCategory", fallbackMethod = "updateCategoryFallback")
    public R<Void> updateCategory(
            @ApiParam(value = "分类ID", required = true) @PathVariable Long categoryId,
            @ApiParam(value = "分类信息", required = true) @RequestBody Categories category) {
        category.setCategoryId(categoryId);
        categoryService.updateCategory(category);
        return R.ok();
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/{categoryId}")
    @CircuitBreaker(name = "deleteCategory", fallbackMethod = "deleteCategoryFallback")
    public R<Void> deleteCategory(
            @ApiParam(value = "分类ID", required = true) @PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return R.ok();
    }

    public R<List<CategoryVO>> getAllCategoriesFallback(Throwable t) {
        return R.error("无法获取所有分类，请稍后再试");
    }

    public R<List<CategoryVO>> getVisibleCategoriesFallback(Throwable t) {
        return R.error("无法获取所有可见分类，请稍后再试");
    }

    public R<CategoryVO> getCategoryByIdFallback(Long categoryId, Throwable t) {
        return R.error("无法获取分类详情，请稍后再试");
    }

    public R<List<CategoryVO>> getChildCategoriesFallback(Long parentId, Throwable t) {
        return R.error("无法获取子分类，请稍后再试");
    }

    public R<Void> addCategoryFallback(Categories category, Throwable t) {
        return R.error("无法新增分类，请稍后再试");
    }

    public R<Void> updateCategoryFallback(Long categoryId, Categories category, Throwable t) {
        return R.error("无法修改分类，请稍后再试");
    }

    public R<Void> deleteCategoryFallback(Long categoryId, Throwable t) {
        return R.error("无法删除分类，请稍后再试");
    }
}
