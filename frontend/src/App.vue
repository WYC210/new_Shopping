<template>
  <el-container class="app-container">
    <el-header height="70px" class="main-header">
      <nav class="nav-header">
        <div class="logo">
          <router-link to="/">E-Commerce</router-link>
        </div>
        <div class="search-bar-header">
          <el-input
            v-model="searchQuery"
            placeholder="搜索商品..."
            @keyup.enter="handleSearch"
            clearable
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch"></el-button>
            </template>
          </el-input>
        </div>
        <div class="nav-links">
          <router-link to="/category" class="nav-item">商品分类</router-link>
          <router-link to="/cart" class="nav-item cart-icon">
            <el-badge :value="cartItemsCount" :hidden="cartItemsCount === 0" :max="99" type="danger">
              <el-icon><ShoppingCart /></el-icon>
            </el-badge>
            <span>购物车</span>
          </router-link>

          <template v-if="isAuthenticated">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-profile">
                <el-avatar :size="32" src="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixlib=rb-4.0.3&auto=format&fit=crop&w=80&h=80" />
                <span class="username">用户名</span>
                <el-icon><CaretBottom /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="coupons">优惠券</el-dropdown-item>
                  <el-dropdown-item command="address">收货地址</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-item auth-link">登录</router-link>
            <router-link to="/register" class="nav-item auth-link">注册</router-link>
          </template>
        </div>
      </nav>
    </el-header>

    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <el-footer height="70px" class="main-footer">
      <div class="footer-content">
        <p>&copy; {{ currentYear }} E-Commerce. All rights reserved.</p>
        <div class="social-links">
          <a href="#" target="_blank" aria-label="WeChat"><el-icon><ChatDotRound /></el-icon></a>
          <a href="#" target="_blank" aria-label="Weibo"><el-icon><Share /></el-icon></a>
          <a href="#" target="_blank" aria-label="QQ"><el-icon><Message /></el-icon></a>
        </div>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { useCartStore } from './stores/cart'
import { ShoppingCart, ChatDotRound, Share, Message, Search, CaretBottom } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const isAuthenticated = computed(() => userStore.isAuthenticated)
const cartItemsCount = computed(() => cartStore.cartItemsCount)
const currentYear = ref(new Date().getFullYear())
const searchQuery = ref('')

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ name: 'SearchResults', query: { q: searchQuery.value } })
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/profile/orders')
      break
    case 'coupons':
      router.push('/profile/coupons')
      break
    case 'address':
      router.push('/profile/address')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
/* 全局布局 */
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

/* 头部样式 */
.main-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-bottom: 1px solid #ebeef5;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.nav-header {
  width: 100%;
  max-width: 1400px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo a {
  font-family: 'Arial Black', Gadget, sans-serif;
  font-size: 28px;
  font-weight: 900;
  text-decoration: none;
  color: #42b983;
  transition: color 0.3s ease;
}

.logo a:hover {
  color: #36a372;
}

.search-bar-header {
  flex-grow: 1;
  max-width: 500px;
  margin: 0 30px;
}

.search-bar-header .el-input {
  border-radius: 25px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.search-bar-header :deep(.el-input__wrapper) {
  border-radius: 25px 0 0 25px;
  padding-left: 15px;
}

.search-bar-header :deep(.el-input-group__append) {
  background-color: #42b983;
  border-radius: 0 25px 25px 0;
  padding: 0 15px;
}

.search-bar-header :deep(.el-button) {
  color: #fff;
}

.nav-links {
  display: flex;
  gap: 25px;
  align-items: center;
}

.nav-links .nav-item {
  text-decoration: none;
  color: #555;
  font-size: 16px;
  font-weight: 500;
  position: relative;
  transition: color 0.3s ease, transform 0.2s ease;
}

.nav-links .nav-item:hover {
  color: #42b983;
  transform: translateY(-2px);
}

.nav-links .nav-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background-color: #42b983;
  transition: width 0.3s ease;
}

.nav-links .nav-item:hover::after {
  width: 100%;
}

.cart-icon {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #555;
}

.cart-icon .el-icon {
  font-size: 20px;
  color: #42b983;
}

.cart-icon .el-badge {
  --el-badge-bg-color: #ff4d4f;
}

/* 登录注册按钮样式 */
.auth-link {
  padding: 8px 15px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.auth-link:hover {
  background-color: #f0f0f0;
  color: #42b983;
}

.auth-link.primary-link {
  background-color: #fff;
  color: #000000;
  font-weight: 600;
  border: 1px solid #42b983;
}

.auth-link.primary-link:hover {
  background-color: #36a372;
  border-color: #36a372;
}

.logout-btn {
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 5px;
}

/* 主内容区 */
.main-content {
  flex-grow: 1;
  padding: 30px 0;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

/* 底部样式 */
.main-footer {
  background-color: #333;
  color: #e0e0e0;
  padding: 20px 40px;
  text-align: center;
  font-size: 14px;
  border-top: 1px solid #444;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-content {
  max-width: 1400px;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.social-links {
  display: flex;
  gap: 15px;
  margin-top: 5px;
}

.social-links a {
  color: #e0e0e0;
  font-size: 20px;
  transition: color 0.3s ease, transform 0.2s ease;
}

.social-links a:hover {
  color: #42b983;
  transform: translateY(-3px);
}

/* 页面过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式布局 */
@media (max-width: 992px) {
  .nav-header {
    flex-wrap: wrap;
    justify-content: center;
    gap: 15px 0;
    padding: 15px 0;
  }

  .search-bar-header {
    order: 3;
    flex-basis: 100%;
    margin: 15px 0 0 0;
  }

  .logo {
    order: 1;
    text-align: left;
    flex-grow: 1;
  }

  .nav-links {
    order: 2;
    flex-wrap: wrap;
    justify-content: flex-end;
    flex-grow: 1;
    gap: 10px;
  }
}

@media (max-width: 768px) {
  .nav-header {
    flex-direction: column;
    gap: 10px;
  }

  .logo {
    text-align: center;
  }

  .search-bar-header {
    order: 2;
    margin-top: 10px;
    max-width: 100%;
  }

  .nav-links {
    order: 3;
    justify-content: center;
    margin-top: 10px;
  }

  .nav-links .nav-item,
  .auth-link,
  .logout-btn {
    font-size: 14px;
    padding: 6px 10px;
  }

  .main-content {
    padding: 20px 15px;
  }

  .footer-content {
    padding: 0 15px;
  }
}

@media (max-width: 576px) {
  .logo a {
    font-size: 22px;
  }

  .nav-links {
    gap: 10px;
  }

  .nav-links .nav-item,
  .auth-link,
  .logout-btn {
    font-size: 13px;
    padding: 5px 8px;
  }

  .main-header {
    padding-left: 15px;
    padding-right: 15px;
  }

  .search-bar-header :deep(.el-input-group__append) {
    padding: 0 10px;
  }
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.user-profile:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #606266;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: #f5f7fa;
  color: #409EFF;
}
</style>
