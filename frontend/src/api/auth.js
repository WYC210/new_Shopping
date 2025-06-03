import apiClient from './client'

export const authApi = {
  // 用户登录
  login(data) {
    console.log('登录请求:', {
      url: '/users/login',
      method: 'post',
      data
    })
    return apiClient.post('/users/login', data).then(response => {
      console.log('登录响应:', response)
      return response
    })
  },

  // 用户注册
  register(data) {
    console.log('注册请求:', {
      url: '/users/register',
      method: 'post',
      data
    })
    
    return apiClient.post('/users/register', data)
      .then(response => {
        console.log('注册响应成功:', response)
        return response
      })
      .catch(error => {
        console.error('注册响应错误:', error)
        
        // 如果服务器返回了响应但业务状态码不是200
        if (error.response && error.response.data) {
          const errorData = error.response.data
          console.error('服务器返回的错误数据:', errorData)
          
          // 抛出带有详细错误信息的错误
          throw new Error(errorData.msg || errorData.message || '注册失败')
        }
        
        // 重新抛出原始错误
        throw error
      })
  },

  // 获取用户信息
  getUserInfo() {
    console.log('获取用户信息请求:', {
      url: '/users/info',
      method: 'get'
    })
    return apiClient.get('/users/info').then(response => {
      console.log('获取用户信息响应:', response)
      return response
    })
  },

  // 更新用户信息
  updateUserInfo(data) {
    console.log('更新用户信息请求:', {
      url: '/users/info',
      method: 'put',
      data
    })
    return apiClient.put('/users/info', data).then(response => {
      console.log('更新用户信息响应:', response)
      return response
    })
  },

  // 修改密码
  changePassword(data) {
    console.log('修改密码请求:', {
      url: '/users/password',
      method: 'put',
      data
    })
    return apiClient.put('/users/password', data).then(response => {
      console.log('修改密码响应:', response)
      return response
    })
  }
} 