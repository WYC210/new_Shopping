import { defineStore } from 'pinia'
import { cartApi } from '../api/cart'
import { productApi } from '../api/product'
import { ElMessage } from 'element-plus'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false
  }),

  getters: {
    // 获取购物车商品总数
    totalItems: (state) => {
      return state.items.reduce((total, item) => total + item.quantity, 0)
    },

    // 获取选中的商品
    selectedItems: (state) => {
      return state.items.filter(item => item.isSelected)
    },

    // 获取选中商品的总数量
    selectedCount: (state) => {
      return state.selectedItems.reduce((total, item) => total + item.quantity, 0)
    },

    // 获取选中商品的总金额
    selectedAmount: (state) => {
      return state.selectedItems.reduce((total, item) => total + (item.price * item.quantity), 0)
    },
    
    // 获取购物车商品总金额
    totalPrice: (state) => {
      return state.items.reduce((total, item) => total + (item.price * item.quantity), 0)
    }
  },

  actions: {
    // 获取购物车列表
    async fetchCartItems() {
      try {
        this.loading = true
        const response = await cartApi.getCartItems()

        if (response.code === 200 && Array.isArray(response.data)) {
          // 处理后端返回的数据
          const cartItems = response.data;

          // 获取每个商品的详细信息
          const itemsWithDetails = await Promise.all(
            cartItems.map(async (item) => {
              try {
                // 获取商品详情
                const productResponse = await productApi.getProductDetail(item.productId);

                if (productResponse.code === 200 && productResponse.data) {
                  const productData = productResponse.data;

                  // 合并商品信息，确保保留skuId
                  return {
                    ...item,
                    name: item.productName || productData.name || '未知商品',
                    price: item.price || productData.price || 99.00,
                    image: productData.images?.[0] || productData.imageUrls?.[0] || 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&h=200',
                    skuId: item.skuId // 确保skuId被保留
                  };
                } else {
                  // 如果获取商品详情失败，使用默认值
                  return {
                    ...item,
                    name: item.productName || '未知商品',
                    price: 99.00,
                    image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&h=200'
                  };
                }
              } catch (error) {
                console.error(`获取商品 ${item.productId} 详情失败:`, error);
                // 出错时使用默认值
                return {
                  ...item,
                  name: item.productName || '未知商品',
                  price: 99.00,
                  image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&h=200'
                };
              }
            })
          );

          this.items = itemsWithDetails;
        } else {
          throw new Error(response.msg || '获取购物车失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '获取购物车失败')
        throw error
      } finally {
        this.loading = false
      }
    },

    // 添加商品到购物车
    async addItem(productId, skuId, quantity) {
      try {
        const response = await cartApi.addToCart(productId, skuId, quantity)
        if (response.code === 200) {
          // 添加成功后刷新购物车列表
          await this.fetchCartItems()
          ElMessage.success('添加成功')
        } else {
          throw new Error(response.msg || '添加商品失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '添加商品失败')
        throw error
      }
    },

    // 更新购物车商品数量
    async updateItemQuantity(cartItemId, quantity) {
      try {
        const response = await cartApi.updateQuantity(cartItemId, quantity)
        if (response.code === 200) {
          // 更新成功后刷新购物车列表
          await this.fetchCartItems()
          ElMessage.success('更新成功')
        } else {
          throw new Error(response.msg || '更新商品数量失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '更新商品数量失败')
        throw error
      }
    },

    // 删除购物车商品
    async removeItem(cartItemId, silent = false) {
      try {
        // 检查cartItemId是否有效
        if (!cartItemId && cartItemId !== 0) {
          if (!silent) ElMessage.error('商品ID无效，无法删除')
          return
        }

        console.log('调用删除API，商品ID:', cartItemId)
        // 确保cartItemId为字符串类型
        const itemId = String(cartItemId);
        const response = await cartApi.removeItem(itemId)
        if (response.code === 200) {
          // 删除成功后刷新购物车列表
          await this.fetchCartItems()
          if (!silent) ElMessage.success('删除成功')
        } else {
          throw new Error(response.msg || '删除商品失败')
        }
      } catch (error) {
        if (!silent) ElMessage.error(error.message || '删除商品失败')
        throw error
      }
    },

    // 清空购物车
    async clearCart() {
      try {
        const response = await cartApi.clearCart()
        if (response.code === 200) {
          this.items = []
          ElMessage.success('购物车已清空')
        } else {
          throw new Error(response.msg || '清空购物车失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '清空购物车失败')
        throw error
      }
    },

    // 选择/取消选择购物车商品
    async toggleItemSelection(cartItemId, isSelected) {
      try {
        const response = await cartApi.toggleItemSelection(cartItemId, isSelected)
        if (response.code === 200) {
          // 更新成功后刷新购物车列表
          await this.fetchCartItems()
        } else {
          throw new Error(response.msg || '更新商品选择状态失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '更新商品选择状态失败')
        throw error
      }
    },

    // 全选/取消全选购物车商品
    async toggleAllItemsSelection(isSelected) {
      try {
        const response = await cartApi.toggleAllSelection(isSelected)
        if (response.code === 200) {
          // 更新成功后刷新购物车列表
          await this.fetchCartItems()
        } else {
          throw new Error(response.msg || '更新商品选择状态失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '更新商品选择状态失败')
        throw error
      }
    },

    // 删除选中的购物车商品
    async removeSelectedItems() {
      try {
        const response = await cartApi.removeSelectedItems()
        if (response.code === 200) {
          // 删除成功后刷新购物车列表
          await this.fetchCartItems()
          ElMessage.success('删除成功')
        } else {
          throw new Error(response.msg || '删除选中商品失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '删除选中商品失败')
        throw error
      }
    }
  }
}) 