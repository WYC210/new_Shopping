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
          @select="handleSelect"
        >
          <el-menu-item index="orders">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="history">
            <el-icon><Clock /></el-icon>
            <span>浏览记录</span>
          </el-menu-item>
          <el-menu-item index="address">
            <el-icon><Location /></el-icon>
            <span>收货地址</span>
          </el-menu-item>
          <el-menu-item index="coupons">
            <el-icon><Ticket /></el-icon>
            <span>我的优惠券</span>
          </el-menu-item>
          <el-menu-item index="info">
            <el-icon><User /></el-icon>
            <span>账号信息</span>
          </el-menu-item>
          <el-menu-item index="password">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
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
import { Document, Location, Ticket, User, Lock, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const activeMenu = ref('orders')

onMounted(() => {
  // 根据当前路由设置激活的菜单项
  const path = route.path.split('/').pop()
  if (path) {
    activeMenu.value = path
  }
})

const handleSelect = (key) => {
  router.push(`/profile/${key}`)
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
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  min-height: calc(100vh - 140px);
}

.profile-nav {
  width: 240px;
  padding: 20px;
  border-right: 1px solid #ebeef5;
}

.user-info {
  text-align: center;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.user-info h3 {
  margin: 10px 0 0;
  font-size: 16px;
  color: #303133;
}

.profile-menu {
  border-right: none;
}

.profile-main {
  flex: 1;
  padding: 20px;
}

:deep(.el-menu-item) {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item.is-active) {
  background: #ecf5ff;
  color: #409EFF;
}

:deep(.el-menu-item:hover) {
  background: #f5f7fa;
}
</style> 