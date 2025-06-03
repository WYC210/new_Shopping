package com.wyc.mapper;

import com.wyc.domain.po.Reviews;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReviewsMapper {
    /**
     * 插入评价
     */
    int insert(Reviews review);

    /**
     * 根据ID查询评价
     */
    Reviews selectById(@Param("reviewId") Long reviewId);

    /**
     * 根据商品ID查询评价列表
     */
    List<Reviews> selectByProductId(@Param("productId") Long productId);

    /**
     * 根据用户ID查询评价列表
     */
    List<Reviews> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据订单ID查询评价列表
     */
    List<Reviews> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 更新评价信息
     */
    int updateById(Reviews review);

    /**
     * 更新评价点赞数
     */
    int updateLikeCount(@Param("reviewId") Long reviewId, @Param("count") int count);

    /**
     * 删除评价
     */
    int deleteById(@Param("reviewId") Long reviewId);

    /**
     * 获取商品的平均评分
     */
    Double getAverageRating(@Param("productId") Long productId);

    /**
     * 获取商品的评价数量
     */
    Integer getReviewCount(@Param("productId") Long productId);
}