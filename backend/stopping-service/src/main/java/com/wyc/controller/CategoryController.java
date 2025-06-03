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

import java.util.List;

@Api(tags = "商品分类接口")
@RestController
@RequestMapping("/wyc/categories")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @ApiOperation("获取所有分类")
    @GetMapping
    public R<List<CategoryVO>> getAllCategories() {
        return R.ok(categoryService.getAllCategories());
    }

    @ApiOperation("获取所有可见分类")
    @GetMapping("/visible")
    public R<List<CategoryVO>> getVisibleCategories() {
        return R.ok(categoryService.getVisibleCategories());
    }

    @ApiOperation("获取分类详情")
    @GetMapping("/{categoryId}")
    public R<CategoryVO> getCategoryById(
            @ApiParam(value = "分类ID", required = true) @PathVariable Long categoryId) {
        return R.ok(categoryService.getCategoryById(categoryId));
    }

    @ApiOperation("获取子分类")
    @GetMapping("/{parentId}/children")
    public R<List<CategoryVO>> getChildCategories(
            @ApiParam(value = "父分类ID", required = true) @PathVariable Long parentId) {
        return R.ok(categoryService.getChildCategories(parentId));
    }

    @ApiOperation("新增分类")
    @PostMapping
    public R<Void> addCategory(
            @ApiParam(value = "分类信息", required = true) @RequestBody Categories category) {
        categoryService.addCategory(category);
        return R.ok();
    }

    @ApiOperation("修改分类")
    @PutMapping("/{categoryId}")
    public R<Void> updateCategory(
            @ApiParam(value = "分类ID", required = true) @PathVariable Long categoryId,
            @ApiParam(value = "分类信息", required = true) @RequestBody Categories category) {
        category.setCategoryId(categoryId);
        categoryService.updateCategory(category);
        return R.ok();
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/{categoryId}")
    public R<Void> deleteCategory(
            @ApiParam(value = "分类ID", required = true) @PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return R.ok();
    }
}
