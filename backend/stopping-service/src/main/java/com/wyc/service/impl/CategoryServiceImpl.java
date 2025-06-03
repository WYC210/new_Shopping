package com.wyc.service.impl;

import com.wyc.domain.po.Categories;
import com.wyc.domain.vo.CategoryVO;
import com.wyc.mapper.CategoriesMapper;
import com.wyc.service.ICategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoriesMapper categoriesMapper;

    @Override
    public List<CategoryVO> getAllCategories() {
        List<Categories> categories = categoriesMapper.selectAll();
        return buildCategoryTree(categories);
    }

    @Override
    public List<CategoryVO> getVisibleCategories() {
        List<Categories> categories = categoriesMapper.selectVisible();
        return buildCategoryTree(categories);
    }

    @Override
    public CategoryVO getCategoryById(Long categoryId) {
        Categories category = categoriesMapper.selectById(categoryId);
        if (category == null) {
            return null;
        }
        return convertToVO(category);
    }

    @Override
    public List<CategoryVO> getChildCategories(Long parentId) {
        List<Categories> children = categoriesMapper.selectByParentId(parentId);
        return children.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addCategory(Categories category) {
        // 设置默认值
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getIsVisible() == null) {
            category.setIsVisible(true);
        }
        if (category.getIsDeleted() == null) {
            category.setIsDeleted(false);
        }

        categoriesMapper.insert(category);
    }

    @Override
    @Transactional
    public void updateCategory(Categories category) {
        Categories existingCategory = categoriesMapper.selectById(category.getCategoryId());
        if (existingCategory == null) {
            throw new RuntimeException("分类不存在");
        }

        categoriesMapper.update(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        // 检查是否有子分类
        List<Categories> children = categoriesMapper.selectByParentId(categoryId);
        if (!children.isEmpty()) {
            throw new RuntimeException("该分类下存在子分类，无法删除");
        }

        categoriesMapper.deleteById(categoryId);
    }

    /**
     * 将Categories转换为CategoryVO
     */
    private CategoryVO convertToVO(Categories category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        vo.setIsVisible(category.getIsVisible());
        vo.setSortOrder(category.getSortOrder().longValue());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    /**
     * 构建分类树形结构
     */
    private List<CategoryVO> buildCategoryTree(List<Categories> categories) {
        // 转换为VO对象
        List<CategoryVO> categoryVOs = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 按父ID分组，使用0L作为null的替代值
        Map<Long, List<CategoryVO>> parentMap = categoryVOs.stream()
                .collect(Collectors.groupingBy(vo -> vo.getParentId() == null ? 0L : vo.getParentId()));

        // 设置子分类
        categoryVOs.forEach(vo -> {
            List<CategoryVO> children = parentMap.get(vo.getCategoryId());
            if (children != null) {
                vo.setChildren(children);
            }
        });

        // 返回顶级分类（parentId为null或0的分类）
        return categoryVOs.stream()
                .filter(vo -> vo.getParentId() == null || vo.getParentId() == 0L)
                .collect(Collectors.toList());
    }
}