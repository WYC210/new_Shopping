import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

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
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue')
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
      component: () => import('../views/Checkout.vue')
    },
    {
      path: '/product/:id',
      name: 'product',
      component: () => import('../views/ProductDetail.vue')
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/Cart.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/profile/Index.vue'),
      children: [
        {
          path: '',
          redirect: '/profile/orders'
        },
        {
          path: 'orders',
          name: 'profile-orders',
          component: () => import('../views/profile/ProfileOrders.vue')
        },
        {
          path: 'address',
          name: 'profile-address',
          component: () => import('../views/profile/ProfileAddress.vue')
        },
        {
          path: 'coupons',
          name: 'profile-coupons',
          component: () => import('../views/profile/ProfileCoupons.vue')
        },
        {
          path: 'history',
          name: 'profile-history',
          component: () => import('../views/profile/ProfileHistory.vue')
        },
        {
          path: 'info',
          name: 'profile-info',
          component: () => import('../views/profile/ProfileInfo.vue')
        },
        {
          path: 'password',
          name: 'profile-password',
          component: () => import('../views/profile/ProfilePassword.vue')
        }
      ]
    }
  ]
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router 