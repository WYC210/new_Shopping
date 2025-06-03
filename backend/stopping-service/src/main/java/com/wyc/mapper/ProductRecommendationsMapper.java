package com.wyc.mapper;

import com.wyc.domain.po.ProductRecommendations;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Date;

@Mapper
public interface ProductRecommendationsMapper {
    /**
     * 插入推荐
     */
    int insert(ProductRecommendations recommendation);

    /**
     * 根据ID查询推荐
     */
    ProductRecommendations selectById(@Param("recommendationId") Long recommendationId);

    /**
     * 根据类型查询推荐列表
     */
    List<ProductRecommendations> selectByType(@Param("type") Integer type);

    /**
     * 根据位置查询推荐列表
     */
    List<ProductRecommendations> selectByPosition(@Param("position") String position);

    /**
     * 查询有效的推荐列表
     */
    List<ProductRecommendations> selectValid(@Param("currentTime") Date currentTime);

    /**
     * 更新推荐
     */
    int updateById(ProductRecommendations recommendation);

    /**
     * 更新推荐状态
     */
    int updateStatus(@Param("recommendationId") Long recommendationId, @Param("status") Integer status);

    /**
     * 删除推荐
     */
    int deleteById(@Param("recommendationId") Long recommendationId);

    /**
     * 根据商品ID查询推荐列表
     */
    List<ProductRecommendations> selectByProductId(@Param("productId") Long productId);

    /**
     * 查询相关商品推荐
     */
    List<ProductRecommendations> selectRelatedProducts(@Param("productId") Long productId,
            @Param("categoryId") Long categoryId);
}