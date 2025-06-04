import apiClient from './client'

export const categoryApi = {
  // 获取所有商品分类
  getCategories() {
    return apiClient.get('/categories')
  },

  // 获取分类详情
  getCategoryById(id) {
    return apiClient.get(`/categories/${id}`)
  }
} 