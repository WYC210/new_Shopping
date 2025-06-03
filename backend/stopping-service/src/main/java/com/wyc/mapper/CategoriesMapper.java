package com.wyc.mapper;

import com.wyc.domain.po.Categories;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CategoriesMapper {
    /**
     * 查询所有分类
     */
    List<Categories> selectAll();

    /**
     * 根据ID查询分类
     */
    Categories selectById(@Param("categoryId") Long categoryId);

    /**
     * 根据父ID查询子分类
     */
    List<Categories> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有可见分类
     */
    List<Categories> selectVisible();

    /**
     * 新增分类
     */
    int insert(Categories category);

    /**
     * 修改分类
     */
    int update(Categories category);

    /**
     * 删除分类
     */
    int deleteById(@Param("categoryId") Long categoryId);
}