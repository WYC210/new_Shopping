<template>
  <div class="page-transition">
    <div class="scan-line"></div>
    <div class="content">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  const scanLine = document.querySelector('.scan-line')
  if (scanLine) {
    scanLine.style.animation = 'scan 1.5s ease-in-out'
  }
})
</script>

<style scoped>
.page-transition {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    to bottom,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transform: translateY(-100%);
  mix-blend-mode: overlay;
  pointer-events: none;
  z-index: 10;
}

.content {
  width: 100%;
  height: 100%;
  position: relative;
  z-index: 1;
}

@keyframes scan {
  0% {
    transform: translateY(-100%);
    opacity: 0;
  }
  20% {
    opacity: 1;
  }
  80% {
    opacity: 1;
  }
  100% {
    transform: translateY(100%);
    opacity: 0;
  }
}
</style> 