import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/Home.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { 
        guest: true // 游客页面，已登录用户会被重定向到首页
      }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue'),
      meta: { 
        guest: true // 游客页面，已登录用户会被重定向到首页
      }
    },
    {
      path: '/category',
      name: 'category',
      component: () => import('../views/Category.vue')
    },
    {
      path: '/category/:id',
      name: 'category-with-id',
      component: () => import('../views/Category.vue')
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('../views/Checkout.vue'),
      meta: { 
        requiresAuth: true // 需要登录
      }
    },
    {
      path: '/product/:id',
      name: 'product',
      component: () => import('../views/ProductDetail.vue')
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/Cart.vue'),
      meta: { 
        requiresAuth: true // 需要登录
      }
    },
    {
      path: '/search',
      name: 'SearchResults',
      component: () => import('../views/Search.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/profile/Index.vue'),
      meta: { 
        requiresAuth: true // 需要登录
      },
      redirect: { name: 'profile-info' },
      children: [
        {
          path: 'info',
          name: 'profile-info',
          component: () => import('../views/profile/ProfileInfo.vue'),
          meta: {
            title: '个人信息',
            requiresAuth: true
          }
        },
        {
          path: 'orders',
          name: 'profile-orders',
          component: () => import('../views/profile/ProfileOrders.vue'),
          meta: {
            title: '我的订单',
            requiresAuth: true
          }
        },
        {
          path: 'address',
          name: 'profile-address',
          component: () => import('../views/profile/ProfileAddress.vue'),
          meta: {
            title: '收货地址',
            requiresAuth: true
          }
        },
        {
          path: 'coupons',
          name: 'profile-coupons',
          component: () => import('../views/profile/ProfileCoupons.vue'),
          meta: {
            title: '我的优惠券',
            requiresAuth: true
          }
        },
        {
          path: 'favorites',
          name: 'profile-favorites',
          component: () => import('../views/profile/ProfileHistory.vue'),
          meta: {
            title: '浏览记录',
            requiresAuth: true
          }
        },
        {
          path: 'signin',
          name: 'profile-signin',
          component: () => import('../views/profile/SignIn.vue'),
          meta: {
            title: '每日签到',
            requiresAuth: true
          }
        }
      ]
    },
    // 添加404页面
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/Home.vue')
    }
  ]
})

// 处理路由错误
const originalPush = router.push
router.push = function push(location) {
  return originalPush.call(this, location).catch(err => {
    if (err.name !== 'NavigationDuplicated') {
      console.error('路由导航错误:', err)
      // 可选：显示错误消息
      // ElMessage.error('页面导航出错')
    }
    return Promise.resolve(err)
  })
}

// 全局导航守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = userStore.isAuthenticated

  // 需要登录的页面，但用户未登录
  if (to.meta.requiresAuth && !isAuthenticated) {
    // 保存目标路径，登录后重定向回来
    localStorage.setItem('redirect_path', to.fullPath)
    next('/login')
    return
  }

  // 游客页面，但用户已登录
  if (to.meta.guest && isAuthenticated) {
    next('/')
    return
  }

  next()
})

export default router 