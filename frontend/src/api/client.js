import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import router from '../router'
import { getFingerprint } from '../utils/fingerprint'

// 创建axios实例
const apiClient = axios.create({
  baseURL: '/wyc',  // 匹配 Vite 代理配置
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const token = userStore.token
    
    if (token) {
      // 确保 token 格式正确
      config.headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`
    }
    
    // 添加浏览器指纹到请求头
    try {
      const fingerprint = getFingerprint()
      config.headers['X-Fingerprint'] = fingerprint
    } catch (error) {
      console.error('添加指纹到请求头失败:', error)
    }
    
    // 修复URL路径，防止出现/wyc/wyc/这样的重复路径
    if (config.url && config.url.startsWith('/wyc/')) {
      config.url = config.url.replace('/wyc/', '/')
    }
    
    // 添加详细的请求日志
    console.log('🚀 发送请求:', {
      url: config.url,
      method: config.method,
      headers: config.headers,
      data: config.data,
      params: config.params,
      timestamp: new Date().toISOString()
    })
    
    return config
  },
  error => {
    console.error('❌ 请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    const res = response.data
    
    // 添加详细的响应日志
    console.log('✅ 收到响应:', {
      url: response.config.url,
      status: response.status,
      data: res,
      headers: response.headers,
      timestamp: new Date().toISOString()
    })
    
    // 检查业务状态码
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    // 添加详细的错误日志
    console.error('❌ 响应错误:', {
      url: error.config?.url,
      status: error.response?.status,
      data: error.response?.data,
      message: error.message,
      timestamp: new Date().toISOString()
    })
    
    // 处理 401 未授权错误
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(new Error('登录已过期，请重新登录'))
    }
    
    // 处理 403 禁止访问错误
    if (error.response?.status === 403) {
      const userStore = useUserStore()
      if (!userStore.isAuthenticated) {
        router.push('/login')
        ElMessage.error('请先登录')
      } else {
        ElMessage.error('没有权限访问该资源')
      }
      return Promise.reject(new Error('没有权限访问该资源'))
    }
    
    const message = error.response?.data?.msg || error.response?.data?.message || '服务器错误'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default apiClient 