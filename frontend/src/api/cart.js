import apiClient from './client'

export const cartApi = {
  // 获取购物车列表
  getCartItems() {
    return apiClient.get('/cart/items')
  },

  // 添加商品到购物车
  addToCart(productId, skuId, quantity) {
    return apiClient.post('/cart/items', {
      productId,
      skuId,
      quantity
    })
  },

  // 更新购物车商品数量
  updateQuantity(cartItemId, quantity) {
    return apiClient.put(`/cart/items/${cartItemId}/quantity`, null, {
      params: { quantity }
    })
  },

  // 删除购物车商品
  removeItem(cartItemId) {
    // 检查cartItemId是否有效
    if (!cartItemId && cartItemId !== 0) {
      console.error('删除购物车商品失败: 无效的商品ID');
      return Promise.reject(new Error('无效的商品ID'));
    }
    // 确保cartItemId为字符串类型
    const itemId = String(cartItemId);
    return apiClient.delete(`/cart/items/${itemId}`);
  },

  // 清空购物车
  clearCart() {
    return apiClient.delete('/cart/items')
  },

  // 获取购物车商品数量
  getCartItemCount() {
    return apiClient.get('/cart/count')
  },

  // 选择/取消选择商品
  toggleItemSelection(cartItemId, isSelected) {
    return apiClient.put(`/cart/items/${cartItemId}/selected`, null, {
      params: { isSelected: isSelected ? 1 : 0 }
    })
  },

  // 全选/取消全选
  toggleAllSelection(isSelected) {
    return apiClient.put('/cart/items/selected', null, {
      params: { isSelected: isSelected ? 1 : 0 }
    })
  },

  // 获取购物车选中商品
  getSelectedCartItems() {
    return apiClient.get('/cart/items/selected')
  },
  
  // 删除选中的购物车商品
  removeSelectedItems() {
    return apiClient.delete('/cart/items/selected')
  }
} 