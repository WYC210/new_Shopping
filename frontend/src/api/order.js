import apiClient from './client'

export const orderApi = {
  // 创建订单
  createOrder(data) {
    return apiClient.post('/orders', data)
  },

  // 获取用户订单列表
  getUserOrders(params) {
    return apiClient.get('/orders/user', { params })
  },

  // 获取订单详情
  getOrderDetail(orderId) {
    return apiClient.get(`/orders/${orderId}`)
  },

  // 取消订单
  cancelOrder(orderId) {
    return apiClient.post(`/orders/${orderId}/cancel`)
  },

  // 确认收货
  confirmReceipt(orderId) {
    return apiClient.post(`/orders/${orderId}/confirm`)
  },

  // 支付订单
  payOrder(orderId, paymentMethod) {
    return apiClient.post(`/orders/${orderId}/pay`, null, {
      params: { paymentMethod }
    })
  },

  // 删除订单
  deleteOrder(orderId) {
    return apiClient.delete(`/orders/${orderId}`)
  }
} 