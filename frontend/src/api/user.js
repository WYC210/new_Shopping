import apiClient from './client'

export const userApi = {
  // 用户登录
  login(data) {
    return apiClient.post('/users/login', data)
  },

  // 用户登出
  logout() {
    return apiClient.post('/users/logout')
  },

  // 用户注册
  register(data) {
    return apiClient.post('/users/register', data)
  },

  // 获取用户详情
  getUserDetails() {
    return apiClient.get('/users/details')
  },

  // 更新用户信息
  updateUserInfo(data) {
    return apiClient.put('/users/profile', data)
  },

  // 修改密码
  updatePassword(data) {
    return apiClient.post('/users/password', data)
  },

  // 获取用户优惠券列表
  getUserCoupons(status) {
    return apiClient.get('/users/coupons', {
      params: { status }
    })
  },

  // 领取优惠券
  claimCoupon(couponId) {
    return apiClient.post(`/users/coupons/${couponId}`)
  },

  // 获取浏览记录
  getBrowsingHistory() {
    return apiClient.get('/users/browsing-history')
  },

  // 记录浏览历史
  recordBrowsing(data) {
    return apiClient.post('/users/browsing-history', null, {
      params: data
    })
  },

  // 查询用户余额
  getBalance() {
    return apiClient.get('/users/balance')
  },

  // 更新用户余额
  updateBalance(data) {
    return apiClient.post('/users/balance', data)
  },

  // ===== 签到相关API =====
  
  // 执行签到
  signIn() {
    return apiClient.post('/sign')
  },

  // 检查今天是否已签到
  checkSignIn() {
    return apiClient.get('/sign/check')
  },

  // 获取当月签到记录
  getMonthSignRecord() {
    return apiClient.get('/sign/month')
  },

  // 获取签到统计信息
  getSignStats() {
    return apiClient.get('/sign/stats')
  }
} 