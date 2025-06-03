import apiClient from './client'

export const couponApi = {
  // 获取优惠券列表
  getCoupons(params) {
    return apiClient.get('/coupon/list', { params })
  },

  // 获取可领取的优惠券列表
  getAvailableCoupons() {
    return apiClient.get('/coupons/available')
  },

  // 领取优惠券
  receiveCoupon(couponId) {
    return apiClient.post(`/coupons/${couponId}/receive`)
  },

  // 获取用户的优惠券列表
  getUserCoupons(status) {
    return apiClient.get('/coupons/user', {
      params: { status }
    })
  },

  // 获取优惠券详情
  getCouponDetail(couponId) {
    return apiClient.get(`/coupons/${couponId}`)
  },

  // 检查优惠券是否可用
  checkCouponAvailable(couponId, amount) {
    return apiClient.get('/coupons/check', {
      params: { couponId, amount }
    })
  },

  // 计算订单优惠金额
  calculateDiscount(couponId, amount) {
    return apiClient.get('/coupons/calculate', {
      params: { couponId, amount }
    })
  }
} 