import apiClient from './client'

export const productApi = {
  // 获取所有分类
  async getCategories(id) {
    console.log('📦 获取商品分类请求:', { id })
    const response = await apiClient.get('/categories', {
      params: id ? { id } : undefined
    })
    console.log('📦 获取商品分类响应:', response)
    return response.data
  },

  // 获取热门商品
  async getHotProducts() {
    console.log('🔥 获取热门商品请求')
    const response = await apiClient.get('/products/hot')
    console.log('🔥 获取热门商品响应:', response)
    return response.data
  },

  // 获取新品
  async getNewProducts() {
    console.log('🆕 获取新品请求')
    const response = await apiClient.get('/products/new')
    console.log('🆕 获取新品响应:', response)
    return response.data
  },

  // 获取商品详情
  getProductDetail(id) {
    console.log('📝 获取商品详情请求:', { id })
    return apiClient.get(`/products/${id}`).then(response => {
      console.log('📝 获取商品详情响应:', response)
      return response
    }).catch(error => {
      console.error('❌ 获取商品详情失败:', error)
      throw error
    })
  },

  // 搜索商品
  async searchProducts(keyword) {
    console.log('🔍 搜索商品请求:', { keyword })
    const response = await apiClient.get('/products/search', {
      params: { keyword }
    })
    console.log('🔍 搜索商品响应:', response)
    return response.data
  },

  // 获取分类下的商品
  async getProductsByCategory(categoryId, params = {}) {
    console.log('📋 获取分类商品请求:', {
      categoryId,
      params: {
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 20,
        minPrice: params.minPrice,
        maxPrice: params.maxPrice,
        sortField: params.sortField,
        sortOrder: params.sortOrder
      }
    })
    
    try {
      const response = await apiClient.get(`/products/category/${categoryId}`, {
        params: {
          pageNum: params.pageNum || 1,
          pageSize: params.pageSize || 20,
          minPrice: params.minPrice,
          maxPrice: params.maxPrice,
          sortField: params.sortField,
          sortOrder: params.sortOrder
        }
      })
      
      console.log('📋 获取分类商品响应:', response)
      return response
    } catch (error) {
      console.error('❌ 获取分类商品失败:', error)
      throw error
    }
  },

  // 获取商品评价列表
  getProductReviews(productId, params) {
    console.log('获取商品评价列表请求:', {
      url: `/products/${productId}/reviews`,
      method: 'get',
      params,
      headers: apiClient.defaults.headers
    })
    return apiClient.get(`/products/${productId}/reviews`, { params }).then(response => {
      console.log('获取商品评价列表响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('获取商品评价列表失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 获取商品评价数量
  async getProductReviewCount(productId) {
    const response = await apiClient.get(`/reviews/products/${productId}/count`)
    return response.data
  },

  // 获取商品平均评分
  async getProductAverageRating(productId) {
    const response = await apiClient.get(`/reviews/products/${productId}/rating`)
    return response.data
  }
} 