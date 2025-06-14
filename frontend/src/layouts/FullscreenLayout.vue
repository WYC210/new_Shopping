<template>
  <div class="fullscreen-layout">
    <transition name="zoom" mode="out-in" appear>
      <slot></slot>
    </transition>
  </div>
</template>

<script setup>
// 无需额外逻辑
</script>

<style scoped>
.fullscreen-layout {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--primary-gradient, linear-gradient(135deg, #1a1a2e 0%, #16213e 100%));
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
</style> 