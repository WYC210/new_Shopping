<template>
  <div class="zigzag-container" ref="container">
    <!-- 背景参照物 -->
    <div class="parallax-elements">
      <div class="floating-shape shape1"></div>
      <div class="floating-shape shape2"></div>
      <div class="floating-shape shape3"></div>
      <div class="grid-overlay"></div>
    </div>

    <div class="sections-wrapper" ref="wrapper">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import gsap from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { Observer } from 'gsap/Observer'

gsap.registerPlugin(ScrollTrigger, Observer)

const container = ref(null)
const wrapper = ref(null)
const isHorizontal = ref(false)

// 存储每个section的位置信息
const sections = ref([])
const currentSection = ref(0)

// 触摸事件相关变量
const touchStart = ref({ x: 0, y: 0 })
const lastTouch = ref({ x: 0, y: 0 })
const touchStartTime = ref(0)

onMounted(() => {
  initSections()
  setupScrollTrigger()
  setupTouchEvents()
  setupParallaxEffects()
})

const initSections = () => {
  sections.value = Array.from(wrapper.value.children).map((section, index) => {
    const rect = section.getBoundingClientRect()
    return {
      element: section,
      index,
      top: rect.top,
      left: rect.left,
      width: rect.width,
      height: rect.height
    }
  })
}

const setupParallaxEffects = () => {
  // 为浮动形状添加动画
  const shapes = document.querySelectorAll('.floating-shape')
  shapes.forEach((shape, i) => {
    gsap.to(shape, {
      x: `random(-20, 20)`,
      y: `random(-20, 20)`,
      rotation: `random(-15, 15)`,
      duration: `random(2, 4)`,
      repeat: -1,
      yoyo: true,
      ease: 'sine.inOut',
      delay: i * 0.2
    })
  })

  // 为网格添加视差效果
  gsap.to('.grid-overlay', {
    x: '-=50%',
    ease: 'none',
    scrollTrigger: {
      trigger: wrapper.value,
      start: 'top top',
      end: 'bottom bottom',
      scrub: 1
    }
  })
}

const setupScrollTrigger = () => {
  // 创建主时间轴
  const tl = gsap.timeline({
    scrollTrigger: {
      trigger: wrapper.value,
      pin: true,
      scrub: 1,
      snap: {
        snapTo: 1 / (sections.value.length - 1),
        duration: { min: 0.2, max: 0.3 },
        delay: 0,
        ease: 'power1.inOut'
      },
      end: () => `+=${wrapper.value.scrollWidth - window.innerWidth}`
    }
  })

  // 添加横向滚动
  tl.to(wrapper.value, {
    x: () => -(wrapper.value.scrollWidth - window.innerWidth),
    ease: 'none'
  })

  // 为每个section添加视差效果
  sections.value.forEach((section, i) => {
    const content = section.element.querySelector('.section-content')
    if (content) {
      gsap.fromTo(content, 
        {
          opacity: 0,
          scale: 0.8,
          x: 100
        },
        {
          opacity: 1,
          scale: 1,
          x: 0,
          scrollTrigger: {
            trigger: section.element,
            containerAnimation: tl,
            start: 'left center',
            end: 'right center',
            scrub: true
          }
        }
      )
    }
  })
}

const setupTouchEvents = () => {
  const handleTouchStart = (e) => {
    const touch = e.touches[0]
    touchStart.value = { x: touch.clientX, y: touch.clientY }
    lastTouch.value = { x: touch.clientX, y: touch.clientY }
    touchStartTime.value = Date.now()
  }

  const handleTouchMove = (e) => {
    e.preventDefault()
    const touch = e.touches[0]
    const deltaX = touch.clientX - lastTouch.value.x
    const deltaY = touch.clientY - lastTouch.value.y
    
    // 判断滚动方向
    if (Math.abs(deltaX) > Math.abs(deltaY)) {
      // 横向滚动
      if (isHorizontal.value) {
        const currentX = gsap.getProperty(wrapper.value, 'x')
        gsap.to(wrapper.value, {
          x: currentX + deltaX,
          duration: 0.1,
          ease: 'power2.out'
        })
      }
    } else {
      // 竖向滚动
      window.scrollBy(0, -deltaY)
    }
    
    lastTouch.value = { x: touch.clientX, y: touch.clientY }
  }

  const handleTouchEnd = (e) => {
    const touchDuration = Date.now() - touchStartTime.value
    const touch = e.changedTouches[0]
    const deltaX = touch.clientX - touchStart.value.x
    const deltaY = touch.clientY - touchStart.value.y
    
    // 快速滑动检测
    if (touchDuration < 300) {
      if (Math.abs(deltaX) > 50 || Math.abs(deltaY) > 50) {
        const nextSection = deltaY > 0 ? currentSection.value - 1 : currentSection.value + 1
        if (nextSection >= 0 && nextSection < sections.value.length) {
          scrollToSection(nextSection)
        }
      }
    }
  }

  container.value.addEventListener('touchstart', handleTouchStart, { passive: false })
  container.value.addEventListener('touchmove', handleTouchMove, { passive: false })
  container.value.addEventListener('touchend', handleTouchEnd, { passive: false })

  // 清理函数
  onUnmounted(() => {
    container.value?.removeEventListener('touchstart', handleTouchStart)
    container.value?.removeEventListener('touchmove', handleTouchMove)
    container.value?.removeEventListener('touchend', handleTouchEnd)
    ScrollTrigger.getAll().forEach(trigger => trigger.kill())
  })
}

const scrollToSection = (index) => {
  const targetSection = sections.value[index]
  if (!targetSection) return

  gsap.to(window, {
    scrollTo: { y: targetSection.element, autoKill: false },
    duration: 1,
    ease: 'power2.inOut'
  })

  if (index % 2 === 0) {
    gsap.to(wrapper.value, {
      x: -targetSection.left,
      duration: 1,
      ease: 'power2.inOut'
    })
  }
}
</script>

<style scoped>
.zigzag-container {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  position: relative;
  background: var(--el-bg-color);
}

.sections-wrapper {
  position: relative;
  height: 100%;
  display: flex;
  will-change: transform;
}

.parallax-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  background: var(--el-color-primary);
  will-change: transform;
}

.shape1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 5%;
  background: linear-gradient(45deg, var(--el-color-primary), var(--el-color-success));
}

.shape2 {
  width: 200px;
  height: 200px;
  bottom: 15%;
  right: 10%;
  background: linear-gradient(-45deg, var(--el-color-warning), var(--el-color-danger));
}

.shape3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 50%;
  background: linear-gradient(135deg, var(--el-color-info), var(--el-color-primary));
}

.grid-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 200%;
  height: 100%;
  background-image: linear-gradient(90deg, 
    rgba(255,255,255,0.03) 1px, 
    transparent 1px
  ),
  linear-gradient(0deg, 
    rgba(255,255,255,0.03) 1px, 
    transparent 1px
  );
  background-size: 50px 50px;
  transform: perspective(1000px) rotateX(60deg);
  transform-origin: top;
  opacity: 0.3;
  will-change: transform;
}

/* 添加一些动画类 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform 0.5s ease;
}

.slide-enter-from {
  transform: translateX(100%);
}

.slide-leave-to {
  transform: translateX(-100%);
}
</style> 