<template>
  <div class="profile-container">
    <div class="profile-content">
      <!-- 左侧导航 -->
      <div class="profile-nav">
        <div class="user-info">
          <el-avatar :size="80" :src="userStore.avatar" />
          <h3>{{ userStore.username }}</h3>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="profile-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="profile-info">
            <el-icon><User /></el-icon>
            <span>个人信息</span>
          </el-menu-item>
          <el-menu-item index="profile-orders">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="profile-address">
            <el-icon><Location /></el-icon>
            <span>收货地址</span>
          </el-menu-item>
          <el-menu-item index="profile-coupons">
            <el-icon><Ticket /></el-icon>
            <span>我的优惠券</span>
          </el-menu-item>
          <el-menu-item index="profile-favorites">
            <el-icon><Clock /></el-icon>
            <span>浏览记录</span>
          </el-menu-item>
          <el-menu-item index="profile-signin">
            <el-icon><Calendar /></el-icon>
            <span>每日签到</span>
            <el-tag v-if="!userStore.signInStatus.todaySigned" type="danger" size="small" effect="dark">未签到</el-tag>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容 -->
      <div class="profile-main">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Document, Location, Ticket, User, Lock, Clock, Star, Calendar } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeMenu = ref('profile-orders')

onMounted(async () => {
  // 根据当前路由设置激活的菜单项
  const path = route.path.split('/').pop()
  if (path) {
    activeMenu.value = path
  }

  // 检查今日签到状态
  if (userStore.isAuthenticated) {
    await userStore.checkSignIn()
  }
})

const handleMenuSelect = (key) => {
  // 使用路由名称进行导航，而不是拼接路径
  router.push({ name: key })
}
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
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  min-height: calc(100vh - 140px);
}

.profile-nav {
  width: 240px;
  padding: 20px;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.user-info {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 20px;
}

.user-info h3 {
  margin: 10px 0 0;
  font-size: 16px;
  color: #fff;
}

.profile-menu {
  border-right: none;
  background: transparent;
}

.profile-main {
  flex: 1;
  padding: 20px;
}

:deep(.el-menu) {
  background-color: transparent;
}

:deep(.el-menu-item) {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 50px;
  line-height: 50px;
  color: rgba(255, 255, 255, 0.7);
}

:deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}
</style> 