<template>
  <div class="fullscreen-layout">
    <slot></slot>
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
}
</style> 