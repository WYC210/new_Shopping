<template>
  <router-view v-slot="{ Component, route }">
    <component 
      :is="route.meta.layout || MainLayout" 
      class="app-container"
    >
      <component :is="Component" />
    </component>
  </router-view>
</template>

<script setup>
import { ref, onMounted, onErrorCaptured } from 'vue'
import MainLayout from './layouts/MainLayout.vue'
import { authService } from './services/auth'
import { ElMessage } from 'element-plus'

// 应用初始化状态
const isAppReady = ref(false)
const errorCount = ref(0)

// 全局错误处理
onErrorCaptured((error, instance, info) => {
  console.warn('应用捕获到错误:', error, info)
  errorCount.value++
  
  // 返回false阻止错误继续传播
  return false
})

// 在应用启动时初始化认证状态
onMounted(async () => {
  try {
    await authService.init()
    isAppReady.value = true
  } catch (error) {
    console.error('应用初始化失败:', error)
    ElMessage.error('应用初始化失败，请刷新页面')
  }
})
</script>

<style scoped>
/* 全局布局 */
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--primary-gradient, linear-gradient(135deg, #1a1a2e 0%, #16213e 100%));
}
</style>
