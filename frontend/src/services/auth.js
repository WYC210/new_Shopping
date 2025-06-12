import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import router from '../router'

/**
 * 认证服务，用于处理用户登录状态检查和初始化
 */
export const authService = {
  /**
   * 初始化认证状态
   * 在应用启动时调用，从本地存储恢复用户会话
   */
  async init() {
    const userStore = useUserStore()
    const cartStore = useCartStore()
    
    try {
      // 尝试从本地存储恢复用户会话
      await userStore.restoreSession()
      
      // 如果用户已登录，获取购物车数据
      if (userStore.isAuthenticated) {
        await cartStore.fetchCartItems()
      }
    } catch (error) {
      console.error('初始化认证状态失败:', error)
    }
  },
  
  /**
   * 检查用户是否已登录，如果未登录则跳转到登录页面
   * @param {string} redirectPath - 登录后重定向的路径
   * @returns {boolean} 用户是否已登录
   */
  checkAuth(redirectPath = '') {
    const userStore = useUserStore()
    
    if (!userStore.isAuthenticated) {
      // 将当前路径保存到本地存储，登录后可以重定向回来
      if (redirectPath) {
        localStorage.setItem('redirect_path', redirectPath)
      }
      
      router.push('/login')
      return false
    }
    
    return true
  },
  
  /**
   * 登录后重定向到之前的页面
   */
  redirectAfterLogin() {
    const redirectPath = localStorage.getItem('redirect_path')
    
    if (redirectPath) {
      localStorage.removeItem('redirect_path')
      router.push(redirectPath)
    } else {
      router.push('/')
    }
  }
} 