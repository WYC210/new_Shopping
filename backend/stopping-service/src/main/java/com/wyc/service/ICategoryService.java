package com.wyc.service;

import com.wyc.domain.po.Categories;
import com.wyc.domain.vo.CategoryVO;
import java.util.List;

public interface ICategoryService {
    /**
     * 获取所有分类（树形结构）
     */
    List<CategoryVO> getAllCategories();

    /**
     * 获取所有可见分类（树形结构）
     */
    List<CategoryVO> getVisibleCategories();

    /**
     * 获取分类详情
     */
    CategoryVO getCategoryById(Long categoryId);

    /**
     * 获取子分类
     */
    List<CategoryVO> getChildCategories(Long parentId);

    /**
     * 新增分类
     */
    void addCategory(Categories category);

    /**
     * 修改分类
     */
    void updateCategory(Categories category);

    /**
     * 删除分类
     */
    void deleteCategory(Long categoryId);
}