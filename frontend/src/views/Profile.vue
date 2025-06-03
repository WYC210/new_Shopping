<template>
  <div class="profile-container">
    <div class="profile-content">
      <!-- Sidebar Navigation -->
      <div class="profile-sidebar">
        <el-menu
          :default-active="activeMenu"
          class="profile-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="info">
            <el-icon><User /></el-icon>
            <span>账号信息</span>
          </el-menu-item>
          <el-menu-item index="password">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </el-menu-item>
          <el-menu-item index="address">
            <el-icon><Location /></el-icon>
            <span>收货地址</span>
          </el-menu-item>
          <el-menu-item index="coupons">
            <el-icon><Ticket /></el-icon>
            <span>我的优惠券</span>
          </el-menu-item>
          <el-menu-item index="orders">
            <el-icon><List /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="favorites">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- Main Content Area -->
      <div class="profile-main">
        <router-view v-if="isLoading" v-loading="true"></router-view>
        <router-view v-else></router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Location, Ticket, List, Star } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeMenu = ref('info')
const isLoading = ref(true)

// 检查用户是否已登录
const checkAuth = () => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    await userStore.getUserInfo()
  } catch (error) {
    ElMessage.error('获取用户信息失败')
    router.push('/login')
  } finally {
    isLoading.value = false
  }
}

const handleMenuSelect = (index) => {
  if (!checkAuth()) return
  router.push(`/profile/${index}`)
}

onMounted(async () => {
  if (!checkAuth()) return
  
  // 设置当前激活的菜单项
  const path = route.path
  const menuIndex = path.split('/').pop()
  if (menuIndex) {
    activeMenu.value = menuIndex
  }
  
  // 获取用户信息
  await fetchUserInfo()
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.profile-content {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 140px);
}

.profile-sidebar {
  width: 240px;
  flex-shrink: 0;
}

.profile-menu {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-right: none;
}

.profile-main {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #409EFF 0%, #36D1DC 100%);
  color: white;
}

:deep(.el-menu-item:hover) {
  background: #f5f7fa;
}

:deep(.el-menu-item.is-active:hover) {
  background: linear-gradient(135deg, #36D1DC 0%, #409EFF 100%);
}
</style> 