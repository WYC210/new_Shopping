import apiClient from './client'

export const recommendationApi = {
  // 获取首页推荐
  getHomeRecommendations() {
    return apiClient.get('/recommendations/home')
  },
  
  // 获取分类推荐
  getCategoryRecommendations(categoryId) {
    return apiClient.get(`/recommendations/category/${categoryId}`)
  },
  
  // 获取相关商品推荐
  getRelatedProducts(productId) {
    return apiClient.get(`/recommendations/related/${productId}`)
  },
  
  // 获取个性化推荐
  getPersonalizedRecommendations(userId) {
    return apiClient.get(`/recommendations/personalized/${userId}`)
  },
  
  // 获取购物车页面推荐商品
  getCartRecommendations() {
    return apiClient.get('/recommendations/cart')
  }
} 