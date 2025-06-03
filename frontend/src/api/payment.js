import apiClient from './client'

export const paymentApi = {
  // 创建支付订单
  createPayment(orderId, paymentMethod) {
    return apiClient.post('/payment/create', {
      orderId,
      paymentMethod
    })
  },

  // 获取支付状态
  getPaymentStatus(paymentId) {
    return apiClient.get(`/payment/${paymentId}/status`)
  },

  // 取消支付
  cancelPayment(paymentId) {
    return apiClient.post(`/payment/${paymentId}/cancel`)
  },

  // 获取支付方式列表
  getPaymentMethods() {
    return apiClient.get('/payment/methods')
  },

  // 获取支付记录
  getPaymentHistory(params) {
    return apiClient.get('/payment/history', { params })
  },

  // 退款申请
  requestRefund(paymentId, data) {
    return apiClient.post(`/payment/${paymentId}/refund`, data)
  },

  // 获取退款状态
  getRefundStatus(refundId) {
    return apiClient.get(`/payment/refund/${refundId}/status`)
  }
} 