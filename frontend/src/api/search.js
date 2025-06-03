import apiClient from './client'

export const searchApi = {
  // 搜索商品
  searchProducts(params) {
    return apiClient.get('/search/products', { params })
  },

  // 获取搜索建议
  getSearchSuggestions(keyword) {
    return apiClient.get('/search/suggestions', {
      params: { keyword }
    })
  },

  // 获取热门搜索
  getHotSearches() {
    return apiClient.get('/search/hot')
  },

  // 获取搜索历史
  getSearchHistory() {
    return apiClient.get('/search/history')
  },

  // 清除搜索历史
  clearSearchHistory() {
    return apiClient.delete('/search/history')
  }
} 