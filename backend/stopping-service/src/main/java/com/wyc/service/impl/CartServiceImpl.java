package com.wyc.service.impl;

import com.wyc.domain.po.CartItems;
import com.wyc.domain.po.Products;
import com.wyc.domain.po.ProductSkus;
import com.wyc.domain.vo.CartItemVO;
import com.wyc.exception.ServiceException;
import com.wyc.mapper.CartItemsMapper;
import com.wyc.mapper.ProductsMapper;
import com.wyc.mapper.ProductSkusMapper;
import com.wyc.service.ICartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartItemsMapper cartItemsMapper;

    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private ProductSkusMapper productSkusMapper;

    @Override
    @Transactional
    public Long addToCart(Long userId, Long productId, Long skuId, Integer quantity) {
        // 1. 验证商品是否存在
        Products product = productsMapper.selectById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }

        // 2. 验证库存
        if (product.getStock() < quantity) {
            throw new ServiceException("商品库存不足");
        }

        // 3. 检查购物车是否已存在该商品（不管is_deleted）
        Long actualSkuId = skuId != null ? skuId : productId;
        CartItems existingItem = cartItemsMapper.selectByUserIdAndSkuIdAll(userId, actualSkuId);
        if (existingItem != null) {
            if (Boolean.TRUE.equals(existingItem.getIsDeleted())) {
                // 复活逻辑删除的购物车项
                cartItemsMapper.recoverItem(userId, actualSkuId, quantity);
                return existingItem.getItemId();
            } else {
                // 已存在，更新数量
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                existingItem.setUpdateTime(LocalDateTime.now());
                cartItemsMapper.updateById(existingItem);
                return existingItem.getItemId();
            }
        }

        // 4. 创建新的购物车项
        CartItems cartItem = new CartItems();
        cartItem.setUserId(userId);
        cartItem.setProductId(productId);
        cartItem.setSkuId(actualSkuId);
        cartItem.setProductName(product.getName());
        cartItem.setQuantity(quantity);
        cartItem.setIsSelected(true);
        cartItem.setIsDeleted(false);
        cartItem.setCreateTime(LocalDateTime.now());
        cartItem.setUpdateTime(LocalDateTime.now());

        cartItemsMapper.insert(cartItem);
        return cartItem.getItemId();
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        // 1. 验证购物车项是否存在
        CartItems cartItem = cartItemsMapper.selectById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new ServiceException("购物车项不存在");
        }

        // 2. 验证库存
        Products product = productsMapper.selectById(cartItem.getProductId());
        if (product.getStock() < quantity) {
            throw new ServiceException("商品库存不足");
        }

        // 3. 更新数量
        cartItemsMapper.updateQuantity(cartItemId, quantity);
    }

    @Override
    @Transactional
    public void updateSelected(Long userId, Long cartItemId, Integer isSelected) {
        // 1. 验证购物车项是否存在
        CartItems cartItem = cartItemsMapper.selectById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new ServiceException("购物车项不存在");
        }

        // 2. 更新选中状态
        cartItemsMapper.updateSelected(cartItemId, isSelected);
    }

    @Override
    @Transactional
    public void updateAllSelected(Long userId, Integer isSelected) {
        cartItemsMapper.updateAllSelected(userId, isSelected);
    }

    @Override
    @Transactional
    public void deleteCartItem(Long userId, Long cartItemId) {
        // 1. 验证购物车项是否存在
        CartItems cartItem = cartItemsMapper.selectById(cartItemId);
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            throw new ServiceException("购物车项不存在");
        }

        // 2. 删除购物车项
        cartItemsMapper.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartItemsMapper.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteSelected(Long userId) {
        cartItemsMapper.deleteSelectedByUserId(userId);
    }

    @Override
    public List<CartItems> getCartItems(Long userId) {
        return cartItemsMapper.selectByUserId(userId);
    }

    @Override
    public List<CartItems> getSelectedItems(Long userId) {
        List<CartItems> allItems = cartItemsMapper.selectByUserId(userId);
        return allItems.stream()
                .filter(CartItems::getIsSelected)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addToCart(CartItems cartItem) {
        // 确保SKU ID有值
        Long actualSkuId = cartItem.getSkuId() != null ? cartItem.getSkuId() : cartItem.getProductId();

        // 检查购物车中是否已存在该商品SKU
        List<CartItems> existingItems = cartItemsMapper.selectByUserIdAndProductId(
                cartItem.getUserId(), actualSkuId);

        CartItems existingItem = null;
        if (existingItems != null && !existingItems.isEmpty()) {
            existingItem = existingItems.get(0);
        }

        if (existingItem != null) {
            // 如果已存在，更新数量
            existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
            existingItem.setUpdateTime(LocalDateTime.now());
            cartItemsMapper.updateById(existingItem);
        } else {
            // 如果不存在，新增购物车项
            // 获取商品信息
            Products product = productsMapper.selectById(cartItem.getProductId());
            if (product == null) {
                throw new ServiceException("商品不存在");
            }

            cartItem.setProductName(product.getName());
            cartItem.setSkuId(actualSkuId);
            cartItem.setIsSelected(true);
            cartItem.setIsDeleted(false);
            cartItem.setCreateTime(LocalDateTime.now());
            cartItem.setUpdateTime(LocalDateTime.now());
            cartItemsMapper.insert(cartItem);
        }
    }

    @Override
    @Transactional
    public void updateCartItem(CartItems cartItem) {
        cartItem.setUpdateTime(LocalDateTime.now());
        cartItemsMapper.updateById(cartItem);
    }

    @Override
    @Transactional
    public void removeFromCart(Long itemId, Long userId) {
        cartItemsMapper.deleteById(itemId);
    }
}
