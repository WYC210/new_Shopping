import { defineStore } from 'pinia'
import { authApi } from '../api/auth'
import { ElMessage } from 'element-plus'
import router from '../router'


export const useUserStore = defineStore('user', {
  state: () => {
    // Safely parse user data from localStorage
    let user = null
    try {
      const storedUser = localStorage.getItem('user')
      user = storedUser ? JSON.parse(storedUser) : null
    } catch (error) {
      console.error('Error parsing user data from localStorage:', error)
      user = null
    }

    return {
      user,
      token: localStorage.getItem('token') || null,
      isAuthenticated: !!localStorage.getItem('token')
    }
  },
  
  getters: {
    isLoggedIn: (state) => !!state.token && !!state.user
  },
  
  actions: {
    setUser(user) {
      this.user = user
      this.isAuthenticated = true
      if (user) {
        localStorage.setItem('user', JSON.stringify(user))
      } else {
        localStorage.removeItem('user')
      }
    },
    
    setToken(token) {
      // 确保 token 格式正确
      const formattedToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`
      this.token = formattedToken
      localStorage.setItem('token', formattedToken)
    },
    
    logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    },
    
    async login(credentials) {
      try {
        const response = await authApi.login(credentials)
        if (response.code === 200) {
          // 从 msg 字段获取 token
          const token = response.msg
          this.setToken(token)
          
          // 直接设置用户名，不请求用户信息
          this.setUser({ username: credentials.username })
          
          ElMessage.success('登录成功')
          return { token }
        } else {
          throw new Error(response.msg || '登录失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
        throw error
      }
    },

    async register(data) {
      try {
        console.log('Store开始注册:', data.username);
        const response = await authApi.register(data);
        console.log('注册响应:', response);
        
        // 检查业务状态码
        if (response.code === 200) {
          console.log('注册成功');
          return response.data;
        } else {
          // 业务错误
          console.error('注册业务错误:', response);
          throw new Error(response.msg || '注册失败');
        }
      } catch (error) {
        // 记录详细错误
        console.error('注册异常:', error);
        
        // 如果错误对象中有响应数据，尝试提取错误信息
        if (error.response?.data) {
          const errorData = error.response.data;
          throw new Error(errorData.msg || errorData.message || '注册失败');
        }
        
        // 传递原始错误
        throw error;
      }
    },

    async getUserInfo() {
      try {
        const response = await authApi.getUserInfo()
        if (response.code === 200) {
          this.setUser(response.data)
          return response.data
        } else {
          throw new Error(response.msg || '获取用户信息失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '获取用户信息失败')
        throw error
      }
    },

    async updateUserInfo(data) {
      try {
        const response = await authApi.updateUserInfo(data)
        if (response.code === 200) {
          this.setUser(response.data)
          ElMessage.success('更新成功')
          return response.data
        } else {
          throw new Error(response.msg || '更新用户信息失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '更新用户信息失败')
        throw error
      }
    },

    async changePassword(data) {
      try {
        const response = await authApi.changePassword(data)
        if (response.code === 200) {
          ElMessage.success('密码修改成功')
          return response.data
        } else {
          throw new Error(response.msg || '密码修改失败')
        }
      } catch (error) {
        ElMessage.error(error.message || '密码修改失败')
        throw error
      }
    },

    // 检查登录状态
    checkAuth() {
      if (!this.token || !this.user) {
        this.logout()
        return false
      }
      return true
    }
  }
}) 