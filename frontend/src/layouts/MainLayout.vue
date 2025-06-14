<template>
  <el-container class="main-layout">
    <TheNavbar />
    
    <el-main class="main-content">
      <transition :name="currentTransition" mode="out-in" appear>
        <keep-alive :include="cachedViews">
          <slot></slot>
        </keep-alive>
      </transition>
    </el-main>
    
    <TheFooter />
  </el-container>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import TheNavbar from '../components/TheNavbar.vue'
import TheFooter from '../components/TheFooter.vue'
import { useProductStore } from '../stores/product'

// 需要缓存的页面组件名称列表
const cachedViews = ['Home', 'Category', 'ProductDetail', 'Cart', 'SearchResults']

// 当前过渡效果
const currentTransition = ref('fade')

// 获取当前路由
const route = useRoute()

// 根据不同路由设置不同的过渡效果
watch(() => route.path, (newPath) => {
  // 根据路径设置不同的过渡效果
  if (newPath.startsWith('/product')) {
    currentTransition.value = 'zoom'
  } else if (newPath.startsWith('/category')) {
    currentTransition.value = 'slide'
  } else if (newPath.startsWith('/profile')) {
    currentTransition.value = 'flip'
  } else {
    currentTransition.value = 'fade'
  }
}, { immediate: true })

// 在布局组件挂载时预加载一些通用数据
const productStore = useProductStore()

onMounted(async () => {
  // 预加载商品分类等全局数据，避免每个页面都重复加载
  try {
    await productStore.fetchCategories()
    console.log('全局分类数据加载完成')
  } catch (error) {
    console.error('全局分类数据加载失败:', error)
  }
})
</script>

<style scoped>
.main-layout {
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
/* 简单淡入淡出效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 1s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 淡入淡出 + 滑动效果 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

/* 缩放效果 */
.zoom-enter-active,
.zoom-leave-active {
  transition: all 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.zoom-enter-from {
  opacity: 0;
  transform: scale(0.95);
}

.zoom-leave-to {
  opacity: 0;
  transform: scale(1.05);
}

/* 滑动效果 */
.slide-enter-active,
.slide-leave-active {
  transition: all 1s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-enter-from {
  transform: translateX(50px);
  opacity: 0;
}

.slide-leave-to {
  transform: translateX(-50px);
  opacity: 0;
}

/* 3D翻转效果 */
.flip-enter-active,
.flip-leave-active {
  transition: all 1.2s cubic-bezier(0.4, 0, 0.2, 1);
  transform-style: preserve-3d;
  backface-visibility: hidden;
}

.flip-enter-from {
  transform: rotateY(-90deg);
  opacity: 0;
}

.flip-leave-to {
  transform: rotateY(90deg);
  opacity: 0;
}
</style> 