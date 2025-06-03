package com.wyc.service;

import com.wyc.domain.po.CartItems;
import java.util.List;

public interface ICartService {
    /**
     * 添加商品到购物车
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param skuId     SKU ID
     * @param quantity  数量
     * @return 购物车项ID
     */
    Long addToCart(Long userId, Long productId, Long skuId, Integer quantity);

    /**
     * 更新购物车商品数量
     *
     * @param userId     用户ID
     * @param cartItemId 购物车项ID
     * @param quantity   数量
     */
    void updateQuantity(Long userId, Long cartItemId, Integer quantity);

    /**
     * 更新购物车商品选中状态
     *
     * @param userId     用户ID
     * @param cartItemId 购物车项ID
     * @param isSelected 是否选中
     */
    void updateSelected(Long userId, Long cartItemId, Integer isSelected);

    /**
     * 更新用户所有购物车商品的选中状态
     *
     * @param userId     用户ID
     * @param isSelected 是否选中
     */
    void updateAllSelected(Long userId, Integer isSelected);

    /**
     * 删除购物车商品
     *
     * @param userId     用户ID
     * @param cartItemId 购物车项ID
     */
    void deleteCartItem(Long userId, Long cartItemId);

    /**
     * 清空用户购物车
     *
     * @param userId 用户ID
     */
    void clearCart(Long userId);

    /**
     * 删除用户选中的购物车商品
     *
     * @param userId 用户ID
     */
    void deleteSelected(Long userId);

    /**
     * 获取用户购物车列表
     *
     * @param userId 用户ID
     * @return 购物车项列表
     */
    List<CartItems> getCartItems(Long userId);

    /**
     * 获取用户选中的购物车商品列表
     *
     * @param userId 用户ID
     * @return 购物车项列表
     */
    List<CartItems> getSelectedItems(Long userId);

    void addToCart(CartItems cartItem);

    void updateCartItem(CartItems cartItem);

    void removeFromCart(Long itemId, Long userId);
}