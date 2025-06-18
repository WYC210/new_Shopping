import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api', // 从环境变量获取API地址
  timeout: 15000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果返回的状态码不是200，说明接口有问题，直接抛出错误
    if (res.code && res.code !== 200) {
      ElMessage({
        message: res.msg || '接口请求错误',
        type: 'error',
        duration: 5 * 1000
      })

      // 401: Token过期或未登录
      if (res.code === 401) {
        // 清除登录信息
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        
        // 跳转到登录页
        setTimeout(() => {
          location.href = '/login'
        }, 1500)
      }
      
      return Promise.reject(new Error(res.msg || '接口请求错误'))
    } else {
      return res
    }
  },
  error => {
    console.error('响应错误', error)
    
    // 显示错误信息
    ElMessage({
      message: error.message || '网络请求错误',
      type: 'error',
      duration: 5 * 1000
    })
    
    return Promise.reject(error)
  }
)

export default service 