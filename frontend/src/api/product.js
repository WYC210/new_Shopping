import apiClient from './client'

export const productApi = {
  // 获取所有分类
  async getCategories(id) {
    const response = await apiClient.get('/categories', {
      params: id ? { id } : undefined
    })
    return response.data
  },

  // 获取热门商品
  async getHotProducts() {
    const response = await apiClient.get('/products/hot')
    return response.data
  },

  // 获取新品
  async getNewProducts() {
    const response = await apiClient.get('/products/new')
    return response.data
  },

  // 获取商品详情
  getProductDetail(id) {
    console.log('获取商品详情请求:', {
      url: `/products/${id}`,
      method: 'get',
      headers: apiClient.defaults.headers
    })
    return apiClient.get(`/products/${id}`).then(response => {
      console.log('获取商品详情响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('获取商品详情失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 搜索商品
  async searchProducts(keyword) {
    const response = await apiClient.get('/products/search', {
      params: { keyword }
    })
    return response.data
  },

  // 获取分类下的商品
  async getProductsByCategory(categoryId, params = {}) {
    console.log('API请求分类商品:', {
      url: `/products/category/${categoryId}`,
      params: {
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 20,
        minPrice: params.minPrice,
        maxPrice: params.maxPrice,
        sortField: params.sortField,
        sortOrder: params.sortOrder
      }
    });
    
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
      });
      
      // 检查响应格式
      const responseData = response.data;
      console.log('API响应分类商品原始数据:', responseData);
      
      // 如果响应直接是数组，返回数组
      if (Array.isArray(responseData)) {
        console.log('API响应是数组，直接返回');
        return responseData;
      }
      
      // 如果响应是对象，检查是否有data字段
      if (responseData && typeof responseData === 'object') {
        if (responseData.data) {
          console.log('API响应是对象，返回data字段');
          return responseData;
        } else if (responseData.code === 200) {
          console.log('API响应是成功状态码，但没有data字段');
          return responseData;
        }
      }
      
      // 默认返回原始响应
      return responseData;
    } catch (error) {
      console.error('API获取分类商品失败:', error);
      throw error;
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