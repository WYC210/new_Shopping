<template>
  <el-header height="70px" class="main-header glass-nav">
    <div class="nav-header">
      <div class="logo">
        <router-link to="/">ShopSphere</router-link>
      </div>
      <div class="search-bar-header">
        <el-input
          v-model="searchQuery"
          placeholder="搜索商品..."
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button type="primary" @click="handleSearch">
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="nav-links">
        <el-dropdown trigger="hover" class="nav-item" @command="handleCategoryCommand">
          <div class="nav-link">
            <el-icon><Menu /></el-icon>
            <span>商品分类</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item 
                v-for="category in categories" 
                :key="category.categoryId" 
                :command="category.categoryId"
              >
                {{ category.name }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-dropdown trigger="hover" class="nav-item">
          <div class="nav-link" @click="goToCart">
            <el-icon><ShoppingCart /></el-icon>
            <span>购物车</span>
            <el-badge :value="cartItemsCount" :hidden="!cartItemsCount" class="cart-badge" />
          </div>
          <template #dropdown>
            <el-dropdown-menu class="cart-dropdown">
              <div v-if="!cartItemsCount" class="empty-cart">
                <el-empty description="购物车是空的" />
              </div>
              <template v-else>
                <div v-for="item in cartItems" :key="item.id" class="cart-item">
                  <el-image :src="item.image" class="cart-item-image" />
                  <div class="cart-item-info">
                    <div class="cart-item-name">{{ item.name }}</div>
                    <div class="cart-item-price">¥{{ item.price }}</div>
                  </div>
                </div>
                <el-dropdown-item divided>
                  <div class="cart-footer">
                    <span>总计: ¥{{ cartTotal }}</span>
                    <el-button type="primary" size="small" @click="goToCart">去结算</el-button>
                  </div>
                </el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <template v-if="isAuthenticated">
          <el-dropdown trigger="hover" class="nav-item" @command="handleCommand">
            <div class="nav-link">
              <el-avatar :size="32" :src="userStore.avatar" />
              <span>{{ userStore.username }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><Document /></el-icon>
                  <span>我的订单</span>
                </el-dropdown-item>
                <el-dropdown-item command="coupons">
                  <el-icon><Ticket /></el-icon>
                  <span>我的优惠券</span>
                </el-dropdown-item>
                <el-dropdown-item command="address">
                  <el-icon><Location /></el-icon>
                  <span>收货地址</span>
                </el-dropdown-item>
                <el-dropdown-item command="history">
                  <el-icon><Clock /></el-icon>
                  <span>浏览记录</span>
                </el-dropdown-item>
                <el-dropdown-item command="signin">
                  <el-icon><Calendar /></el-icon>
                  <span>每日签到</span>
                  <el-tag v-if="!userStore.signInStatus.todaySigned" type="danger" size="small" effect="dark">未签到</el-tag>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-item auth-link">
            <el-icon><User /></el-icon>
            <span>登录</span>
          </router-link>
          <router-link to="/register" class="nav-item auth-link primary-link">
            <el-icon><Plus /></el-icon>
            <span>注册</span>
          </router-link>
        </template>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useCartStore } from '../stores/cart'
import { categoryApi } from '../api/category'
import { 
  ShoppingCart, 
  Search, 
  CaretBottom,
  Menu,
  User,
  Document,
  Ticket,
  Location,
  SwitchButton,
  Plus,
  Calendar,
  Clock
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

// 商品分类数据
const categories = ref([])
const searchQuery = ref('')

// 获取商品分类数据
const fetchCategories = async () => {
  try {
    const res = await categoryApi.getCategories()
    // 检查响应数据结构
    if (res && res.data) {
      // 如果响应中有data字段，使用data字段
      categories.value = Array.isArray(res.data) ? res.data : (res.data.data || [])
    } else {
      // 如果响应直接是数组，直接使用
      categories.value = Array.isArray(res) ? res : []
    }
  } catch (error) {
    console.error('获取商品分类失败:', error)
    categories.value = []
  }
}

// 组件挂载时获取分类数据
onMounted(async () => {
  fetchCategories()
  // 只有在用户已登录的情况下才获取购物车数据和签到状态
  if (userStore.isAuthenticated) {
    cartStore.fetchCartItems()
    await userStore.checkSignIn()
  }
})

// 监听登录状态变化
watch(() => userStore.isAuthenticated, (isLoggedIn) => {
  if (isLoggedIn) {
    // 登录后获取购物车数据
    cartStore.fetchCartItems()
  }
})

const isAuthenticated = computed(() => userStore.isAuthenticated)
const cartItemsCount = computed(() => isAuthenticated.value ? cartStore.totalItems : 0)
const cartItems = computed(() => isAuthenticated.value ? cartStore.items : [])
const cartTotal = computed(() => isAuthenticated.value ? cartStore.totalPrice : 0)

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ name: 'SearchResults', query: { q: searchQuery.value } })
  }
}

const handleCategoryCommand = (categoryId) => {
  if (categoryId) {
    router.push({ name: 'category-with-id', params: { id: categoryId } })
  } else {
    router.push({ name: 'category' })
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
    case 'history':
      router.push('/profile/favorites')
      break
    case 'signin':
      router.push('/profile/signin')
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

const goToCart = () => {
  router.push('/cart')
}
</script>

<style scoped>
/* 头部样式 */
.main-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 70px; /* 确保高度固定 */
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
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.logo a:hover {
  color: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
}

.search-bar-header {
  flex-grow: 1;
  max-width: 500px;
  margin: 0 30px;
}

.search-bar-header .el-input {
  border-radius: 25px;
  overflow: hidden;
}

.search-bar-header :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  padding: 0 15px;
  box-shadow: none;
  height: 40px;
}

.search-bar-header :deep(.el-input__inner) {
  color: #fff;
  height: 40px;
  line-height: 40px;
}

.search-bar-header :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.7);
}

.search-bar-header :deep(.el-input__prefix) {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
}

.search-bar-header :deep(.el-input-group__append) {
  background: transparent;
  border: none;
  padding: 0;
  margin-left: 10px;
}

.search-bar-header :deep(.el-button) {
  border-radius: 20px;
  height: 40px;
  padding: 0 25px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.search-bar-header :deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.search-bar-header :deep(.el-button:active) {
  transform: translateY(0);
}

.nav-links {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-item {
  cursor: pointer;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 20px;
  transition: all 0.3s ease;
  color: #fff;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.1);
}

.nav-link .el-icon {
  font-size: 18px;
}

.cart-badge {
  margin-top: -2px;
}

.cart-dropdown {
  min-width: 300px;
  padding: 10px;
}

.empty-cart {
  padding: 20px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.cart-item-image {
  width: 50px;
  height: 50px;
  border-radius: 4px;
}

.cart-item-info {
  flex: 1;
}

.cart-item-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.cart-item-price {
  font-size: 14px;
  color: #f56c6c;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
}

.auth-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 15px;
  border-radius: 20px;
  transition: all 0.3s ease;
  color: #fff;
  text-decoration: none;
}

.auth-link:hover {
  background: rgba(255, 255, 255, 0.1);
}

.auth-link.primary-link {
  background: rgba(255, 255, 255, 0.2);
}

.auth-link.primary-link:hover {
  background: rgba(255, 255, 255, 0.3);
}

:deep(.el-dropdown-menu) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  color: #333;
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
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

  .search-bar-header {
    max-width: 100%;
  }
  
  .search-bar-header :deep(.el-button) {
    padding: 0 20px;
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
  .auth-link {
    font-size: 14px;
    padding: 6px 10px;
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
  .auth-link {
    font-size: 13px;
    padding: 5px 8px;
  }

  .main-header {
    padding-left: 15px;
    padding-right: 15px;
  }

  .search-bar-header :deep(.el-button) {
    padding: 0 15px;
  }
}
</style> 