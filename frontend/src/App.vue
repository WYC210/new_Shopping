<template>
  <router-view v-slot="{ Component, route }">
    <component 
      :is="route.meta.layout || 'el-container'" 
      class="app-container"
    >
      <template v-if="!route.meta.layout">
        <TheNavbar v-if="isAppReady" />

        <el-main class="main-content">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </el-main>

        <TheFooter v-if="isAppReady" />
      </template>
      <template v-else>
        <component :is="Component" />
      </template>
    </component>
  </router-view>
</template>

<script setup>
import { ref, onMounted, onErrorCaptured } from 'vue'
import TheNavbar from './components/TheNavbar.vue'
import TheFooter from './components/TheFooter.vue'
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

/* 主内容区 */
.main-content {
  flex-grow: 1;
  padding-top: 70px; /* 与header高度一致 */
  padding-bottom: 30px;
  width: 100%;
  position: relative;
  overflow-x: hidden;
}

/* 对于常规内容页面，使用内部容器控制宽度 */
.main-content :deep(.content-container:not(.full-width)) {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 全宽容器不受限制 */
.main-content :deep(.full-width) {
  width: 100%;
  max-width: 100%;
  margin: 0;
  padding: 0;
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
</style>
