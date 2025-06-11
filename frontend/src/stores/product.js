import { defineStore } from 'pinia'
import { productApi } from '../api/product'
import { ElMessage } from 'element-plus'

export const useProductStore = defineStore('product', {
  state: () => ({
    categories: [],
    featuredProducts: [],
    newArrivals: [],
    categoryProducts: [],
    loading: {
      categories: false,
      featured: false,
      newArrivals: false,
      categoryProducts: false,
      detail: false
    },
    currentProduct: null,
    error: null
  }),

  getters: {
    productDetails: (state) => state.currentProduct,
    isLoading: (state) => state.loading,
    hasError: (state) => state.error !== null
  },

  actions: {
    // 获取商品分类
    async fetchCategories() {
      try {
        this.loading.categories = true
        const data = await productApi.getCategories()
        this.categories = data
        return data
      } catch (error) {
        ElMessage.error('获取商品分类失败')
        throw error
      } finally {
        this.loading.categories = false
      }
    },

    // 获取热门商品
    async fetchHotProducts() {
      try {
        this.loading.featured = true
        const data = await productApi.getHotProducts()
        this.featuredProducts = data
        return data
      } catch (error) {
        ElMessage.error('获取热门商品失败')
        throw error
      } finally {
        this.loading.featured = false
      }
    },

    // 获取新品
    async fetchNewProducts() {
      try {
        this.loading.newArrivals = true
        const data = await productApi.getNewProducts()
        this.newArrivals = data
        return data
      } catch (error) {
        ElMessage.error('获取新品失败')
        throw error
      } finally {
        this.loading.newArrivals = false
      }
    },

    // 获取分类商品
    async fetchProductsByCategory(categoryId, params) {
      try {
        this.loading.categoryProducts = true
        const response = await productApi.getProductsByCategory(categoryId, params)
        console.log('分类商品API响应:', response);
        
        // 检查响应是否为数组（直接返回数据）
        if (Array.isArray(response)) {
          this.categoryProducts = response
          return {
            code: 200,
            data: response,
            msg: '操作成功'
          }
        }
        // 检查响应是否为对象（带有code和data字段）
        else if (response && response.code === 200) {
          this.categoryProducts = response.data.list || response.data
          return response
        } else {
          throw new Error(response?.msg || '获取分类商品失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '获取分类商品失败')
        throw error
      } finally {
        this.loading.categoryProducts = false
      }
    },

    // 获取商品详情
    async fetchProductDetail(id) {
      this.loading.detail = true
      this.error = null
      try {
        const response = await productApi.getProductDetail(id)
        if (response.code === 200) {
          // 确保skus字段存在且为数组，且每个sku的attributes字段为对象
          const data = response.data
          if (Array.isArray(data.skus)) {
            data.skus = data.skus.map(sku => ({
              ...sku,
              attributes: sku.attributes || sku.specs || {}
            }))
          }
          this.currentProduct = data
          return data
        } else {
          throw new Error(response.msg || '获取商品详情失败')
        }
      } catch (error) {
        this.error = error.message
        ElMessage.error(error.message || '获取商品详情失败')
        throw error
      } finally {
        this.loading.detail = false
      }
    },

    // 获取商品评价列表
    async fetchProductReviews(productId, params = {}) {
      try {
        const response = await productApi.getProductReviews(productId, params)
        if (response.code === 200) {
          return response.data
        } else {
          throw new Error(response.msg || '获取商品评价失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '获取商品评价失败')
        throw error
      }
    },

    // 清除当前商品数据
    clearCurrentProduct() {
      this.currentProduct = null
      this.error = null
    }
  }
}) 