import apiClient from './client'

export const profileApi = {
  // 获取用户信息
  getUserInfo() {
    console.log('获取用户信息请求:', {
      url: '/users/details',
      method: 'get',
      headers: apiClient.defaults.headers
    })
    return apiClient.get('/users/details').then(response => {
      console.log('获取用户信息响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('获取用户信息失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 更新用户信息
  updateUserInfo(data) {
    console.log('更新用户信息请求:', {
      url: '/users/profile',
      method: 'put',
      data,
      headers: apiClient.defaults.headers
    })
    return apiClient.put('/users/profile', data).then(response => {
      console.log('更新用户信息响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('更新用户信息失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 获取用户地址列表
  getAddresses() {
    console.log('获取地址列表请求:', {
      url: '/addresses',
      method: 'get',
      headers: apiClient.defaults.headers
    })
    return apiClient.get('/addresses').then(response => {
      console.log('获取地址列表响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('获取地址列表失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 添加新地址
  addAddress(data) {
    console.log('添加地址请求:', {
      url: '/addresses',
      method: 'post',
      data,
      headers: apiClient.defaults.headers
    })
    return apiClient.post('/addresses', data).then(response => {
      console.log('添加地址响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('添加地址失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 更新地址
  updateAddress(id, data) {
    console.log('更新地址请求:', {
      url: `/addresses/${id}`,
      method: 'put',
      data,
      headers: apiClient.defaults.headers
    })
    return apiClient.put(`/addresses/${id}`, data).then(response => {
      console.log('更新地址响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('更新地址失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 删除地址
  deleteAddress(id) {
    console.log('删除地址请求:', {
      url: `/addresses/${id}`,
      method: 'delete',
      headers: apiClient.defaults.headers
    })
    return apiClient.delete(`/addresses/${id}`).then(response => {
      console.log('删除地址响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('删除地址失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 获取优惠券列表
  getCoupons() {
    console.log('获取优惠券列表请求:', {
      url: '/coupons',
      method: 'get',
      headers: apiClient.defaults.headers
    })
    return apiClient.get('/users/coupons').then(response => {
      console.log('获取优惠券列表响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('获取优惠券列表失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 获取收藏列表
  getFavorites() {
    console.log('获取收藏列表请求:', {
      url: '/favorites',
      method: 'get',
      headers: apiClient.defaults.headers
    })
    return apiClient.get('/users/favorites').then(response => {
      console.log('获取收藏列表响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('获取收藏列表失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 添加收藏
  addFavorite(productId) {
    console.log('添加收藏请求:', {
      url: '/favorites',
      method: 'post',
      data: { productId },
      headers: apiClient.defaults.headers
    })
    return apiClient.post('/favorites', { productId }).then(response => {
      console.log('添加收藏响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('添加收藏失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 取消收藏
  removeFavorite(productId) {
    console.log('取消收藏请求:', {
      url: `/favorites/${productId}`,
      method: 'delete',
      headers: apiClient.defaults.headers
    })
    return apiClient.delete(`/users/favorites/${productId}`).then(response => {
      console.log('取消收藏响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('取消收藏失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 获取订单列表
  getOrders(params) {
    console.log('获取订单列表请求:', {
      url: '/orders/user',
      method: 'get',
      params,
      headers: apiClient.defaults.headers
    })
    return apiClient.get('/orders/user', { params }).then(response => {
      console.log('获取订单列表响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      
      // 返回标准化的响应数据，支持分页
      if (response.data && response.data.code === 200) {
        const pageData = response.data.data || {};
        return {
          code: response.data.code,
          msg: response.data.msg,
          data: pageData.records || [],
          total: pageData.total || 0,
          pages: pageData.pages || 1,
          current: pageData.current || 1
        };
      }
      
      return response.data;
    }).catch(error => {
      console.error('获取订单列表失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 获取订单详情
  getOrderDetail(orderId) {
    console.log('获取订单详情请求:', {
      url: `/orders/${orderId}`,
      method: 'get',
      headers: apiClient.defaults.headers
    })
    return apiClient.get(`/orders/${orderId}`).then(response => {
      console.log('获取订单详情响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('获取订单详情失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 取消订单
  cancelOrder(orderId) {
    console.log('取消订单请求:', {
      url: `/orders/${orderId}/cancel`,
      method: 'post',
      headers: apiClient.defaults.headers
    })
    return apiClient.post(`/orders/${orderId}/cancel`).then(response => {
      console.log('取消订单响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('取消订单失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 确认收货
  confirmOrder(orderId) {
    console.log('确认收货请求:', {
      url: `/orders/${orderId}/confirm`,
      method: 'post',
      headers: apiClient.defaults.headers
    })
    return apiClient.post(`/orders/${orderId}/confirm`).then(response => {
      console.log('确认收货响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('确认收货失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 获取浏览记录
  getBrowsingHistory() {
    console.log('获取浏览记录请求:', {
      url: '/users/browsing-history',
      method: 'get',
      headers: apiClient.defaults.headers
    })
    return apiClient.get('/users/browsing-history').then(response => {
      console.log('获取浏览记录响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('获取浏览记录失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  },

  // 删除浏览记录
  deleteBrowsingHistory(recordId) {
    console.log('删除浏览记录请求:', {
      url: `/users/browsing-history/${recordId}`,
      method: 'delete',
      headers: apiClient.defaults.headers
    })
    return apiClient.delete(`/users/browsing-history/${recordId}`).then(response => {
      console.log('删除浏览记录响应:', {
        status: response.status,
        data: response.data,
        headers: response.headers
      })
      return response
    }).catch(error => {
      console.error('删除浏览记录失败:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error
    })
  }
} 