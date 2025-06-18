import request from '@/utils/request'
import axios from 'axios'

// 创建单独的数字大屏API请求实例，直接访问9099端口
const dashboardService = axios.create({
  baseURL: 'http://localhost:9099',
  timeout: 10000
})

// 响应拦截器
dashboardService.interceptors.response.use(
  res => {
    const data = res.data
    // 检查返回的数据是否符合预期格式
    if (data && data.code !== undefined) {
      return data
    }
    // 如果不符合预期格式，手动构造一个统一的返回结构
    return {
      code: res.status === 200 ? 200 : res.status,
      msg: res.statusText || '请求成功',
      data: data
    }
  },
  error => {
    console.error('数字大屏请求错误', error)
    return Promise.reject(error)
  }
)

// 商城API - 获取数字大屏总览数据
export function getShopDashboardOverview() {
  return dashboardService({
    url: '/wyc/dashboard/overview',
    method: 'get'
  })
}

// 商城API - 获取城市用户分布数据
export function getShopCityDistribution() {
  return dashboardService({
    url: '/wyc/dashboard/city-distribution',
    method: 'get'
  })
}

// 商城API - 获取数字大屏趋势数据
export function getShopDashboardTrend(startDate, endDate) {
  return dashboardService({
    url: '/wyc/dashboard/trend',
    method: 'get',
    params: { startDate, endDate }
  })
} 