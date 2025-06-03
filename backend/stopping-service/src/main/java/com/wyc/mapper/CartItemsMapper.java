package com.wyc.mapper;

import com.wyc.domain.po.CartItems;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CartItemsMapper {
    /**
     * 插入购物车项
     */
    int insert(CartItems cartItem);

    /**
     * 根据ID查询购物车项
     */
    CartItems selectById(@Param("cartItemId") Long cartItemId);

    /**
     * 根据用户ID查询购物车项列表
     */
    List<CartItems> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和商品ID查询购物车项列表
     */
    List<CartItems> selectByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    /**
     * 根据购物车项ID和用户ID查询购物车项
     */
    CartItems selectByItemIdAndUserId(@Param("itemId") Long itemId, @Param("userId") Long userId);

    /**
     * 更新购物车项
     */
    int updateById(CartItems cartItem);

    /**
     * 更新购物车项数量
     */
    int updateQuantity(@Param("cartItemId") Long cartItemId, @Param("quantity") Integer quantity);

    /**
     * 更新购物车项选中状态
     */
    int updateSelected(@Param("cartItemId") Long cartItemId, @Param("isSelected") Integer isSelected);

    /**
     * 更新所有购物车项选中状态
     */
    int updateAllSelected(@Param("userId") Long userId, @Param("isSelected") Integer isSelected);

    /**
     * 软删除购物车项（通过ID）
     */
    @Update("UPDATE CartItems SET is_deleted = TRUE, updated_at = NOW() WHERE cart_item_id = #{cartItemId} AND is_deleted = FALSE")
    int deleteById(@Param("cartItemId") Long cartItemId);

    /**
     * 软删除用户的所有购物车项
     */
    @Update("UPDATE CartItems SET is_deleted = TRUE, updated_at = NOW() WHERE user_id = #{userId} AND is_deleted = FALSE")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 软删除用户选中的购物车项
     */
    @Update("UPDATE CartItems SET is_deleted = TRUE, updated_at = NOW() WHERE user_id = #{userId} AND is_selected = 1 AND is_deleted = FALSE")
    int deleteSelectedByUserId(@Param("userId") Long userId);

    /**
     * 更新购物车项
     */
    int update(CartItems cartItem);

    /**
     * 软删除购物车项
     */
    int softDelete(@Param("itemId") Long itemId, @Param("userId") Long userId);
}