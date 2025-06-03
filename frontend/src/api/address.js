import apiClient from './client'

export const addressApi = {
  // 获取地址列表
  getAddresses() {
    return apiClient.get('/addresses')
  },

  // 获取默认地址
  getDefaultAddress() {
    return apiClient.get('/addresses/default')
  },

  // 添加地址
  addAddress(data) {
    return apiClient.post('/addresses', data)
  },

  // 更新地址
  updateAddress(addressId, data) {
    return apiClient.put(`/addresses/${addressId}`, data)
  },

  // 删除地址
  deleteAddress(addressId) {
    return apiClient.delete(`/addresses/${addressId}`)
  },

  // 设置默认地址
  setDefaultAddress(addressId) {
    return apiClient.put(`/addresses/${addressId}/default`)
  },

  // 获取地址详情
  getAddressDetail(addressId) {
    return apiClient.get(`/addresses/${addressId}`)
  }
} 