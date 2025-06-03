<template>
  <div class="poster-wrapper">
    <div class="poster-container" ref="poster">
      <!-- 背景装饰形状 -->
      <div class="background-shapes">
        <div class="shape shape-1" ref="shape1"></div>
        <div class="shape shape-2" ref="shape2"></div>
        <div class="shape shape-3" ref="shape3"></div>
      </div>

      <!-- 浮动装饰元素 -->
      <div class="floating-elements">
        <div 
          class="floating-dot" 
          v-for="(dot, index) in floatingDots" 
          :key="index" 
          :style="{ left: dot.x + 'px', top: dot.y + 'px' }" 
          :ref="el => setDotRef(el, index)"
        ></div>
      </div>

      <div class="content">
        <!-- 头部信息 -->
        <div class="header" ref="header">
          <h1 class="name" ref="name">{{ profileData.name }}</h1>
          <p class="title" ref="title">{{ profileData.title }}</p>
        </div>

        <!-- 主要内容 -->
        <div class="main-content">
          <!-- 个人信息 -->
          <div class="profile-section" ref="profileSection">
            <div 
              class="profile-item" 
              v-for="(item, index) in profileData.info" 
              :key="index" 
              :ref="el => setProfileItemRef(el, index)"
            >
              <div class="icon">{{ item.icon }}</div>
              <div>
                <div class="label">{{ item.label }}</div>
                <div class="value">{{ item.value }}</div>
              </div>
            </div>
          </div>

          <!-- 技能部分 -->
          <div class="skills-section" ref="skillsSection">
            <h3 class="skills-title">技能专长</h3>
            <div class="skill-item" v-for="(skill, index) in profileData.skills" :key="index">
              <span class="skill-name">{{ skill.name }}</span>
              <div class="skill-bar">
                <div 
                  class="skill-progress" 
                  :ref="el => setSkillBarRef(el, index)"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 统计数据（优化布局） -->
        <div class="stats" ref="stats">
          <div class="stats-container">
            <div
              class="stat-item"
              v-for="(stat, index) in profileData.stats"
              :key="index"
            >
              <div class="stat-number" :ref="el => setStatNumberRef(el, index)">{{ stat.number }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </div>

        <!-- 操作按钮（固定底部布局） -->
        <div class="actions" ref="actions" :style="{ position: 'fixed', bottom: '0', left: '0', right: '0', zIndex: '100' }">
          <button class="action-btn primary" @click="downloadResume">
            <span class="btn-icon">📄</span>
            下载简历
          </button>
          <button class="action-btn secondary" @click="copyContact">
            <span class="btn-icon">📋</span>
            复制联系方式
          </button>
          <button class="action-btn accent" @click="$emit('switch-to-projects')">
            <span class="btn-icon">🚀</span>
            查看作品
          </button>
        </div>
      </div>
    </div>

    <!-- 成功提示 -->
    <Transition name="toast">
      <div v-if="showToast" class="toast">
        <span class="toast-icon">✅</span>
        {{ toastMessage }}
      </div>
    </Transition>
  </div>
</template>
<script>
import { ref, onMounted, nextTick, watch, computed, onUnmounted } from 'vue'
import { gsap } from 'gsap'

export default {
  name: 'PersonalProfilePoster',
  
  props: {
    profileData: {
      type: Object,
      required: true,
      default: () => ({
        name: "张小明",
        title: "全栈开发工程师",
        info: [],
        skills: [],
        stats: []
      })
    },
    theme: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'dark', 'colorful'].includes(value)
    },
    animationSpeed: {
      type: Number,
      default: 1,
      validator: (value) => value > 0 && value <= 3
    }
  },

  emits: ['update-profile', 'switch-to-projects', 'contact-clicked'],

  setup(props, { emit }) {
    // 模板引用
    const poster = ref(null)
    const header = ref(null)
    const name = ref(null)
    const title = ref(null)
    const profileSection = ref(null)
    const skillsSection = ref(null)
    const stats = ref(null)
    const actions = ref(null)
    const shape1 = ref(null)
    const shape2 = ref(null)
    const shape3 = ref(null)
    
    // 数组引用
    const profileItemRefs = ref([])
    const skillBarRefs = ref([])
    const statNumberRefs = ref([])
    const dotRefs = ref([])

    // 响应式数据
    const showToast = ref(false)
    const toastMessage = ref('')
    const isAnimating = ref(false)
    const floatingDots = ref([])

    // 设置数组引用的方法
    const setProfileItemRef = (el, index) => {
      if (el) profileItemRefs.value[index] = el
    }
    
    const setSkillBarRef = (el, index) => {
      if (el) skillBarRefs.value[index] = el
    }
    
    const setStatNumberRef = (el, index) => {
      if (el) statNumberRefs.value[index] = el
    }
    
    const setDotRef = (el, index) => {
      if (el) dotRefs.value[index] = el
    }

    // 计算属性 - 主题样式
    const themeStyles = computed(() => {
      const themes = {
        default: {
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          accent: '#4ecdc4'
        },
        dark: {
          background: 'linear-gradient(135deg, #2c3e50 0%, #34495e 100%)',
          accent: '#e74c3c'
        },
        colorful: {
          background: 'linear-gradient(135deg, #ff6b6b 0%, #ffa500 50%, #4ecdc4 100%)',
          accent: '#667eea'
        }
      }
      return themes[props.theme] || themes.default
    })

    // 生成随机浮动点
    const generateFloatingDots = () => {
      const dots = []
      const count = window.innerWidth < 768 ? 8 : 15
      
      for (let i = 0; i < count; i++) {
        dots.push({
          x: Math.random() * window.innerWidth,
          y: Math.random() * window.innerHeight
        })
      }
      floatingDots.value = dots
    }

    // 动画初始化
    const initAnimations = () => {
      if (isAnimating.value) return
      isAnimating.value = true

      const timeline = gsap.timeline({
        onComplete: () => {
          isAnimating.value = false
        }
      })

      const speed = props.animationSpeed

      // 设置初始状态
      gsap.set([name.value, title.value], { y: -50, opacity: 0 })
      gsap.set(profileItemRefs.value, { x: -100, opacity: 0 })
      gsap.set(skillsSection.value, { x: 100, opacity: 0 })
      gsap.set([stats.value, actions.value], { y: 50, opacity: 0 })
      gsap.set(skillBarRefs.value, { scaleX: 0, transformOrigin: 'left' })

      // 执行动画序列
      timeline
        .to(name.value, {
          duration: 1.2 / speed,
          y: 0,
          opacity: 1,
          ease: "back.out(1.7)"
        })
        .to(title.value, {
          duration: 0.8 / speed,
          y: 0,
          opacity: 1,
          ease: "power2.out"
        }, "-=0.6")
        .to(profileItemRefs.value, {
          duration: 0.8 / speed,
          x: 0,
          opacity: 1,
          stagger: 0.1 / speed,
          ease: "power2.out"
        }, "-=0.4")
        .to(skillsSection.value, {
          duration: 0.8 / speed,
          x: 0,
          opacity: 1,
          ease: "power2.out"
        }, "-=0.6")
        .to(skillBarRefs.value, {
          duration: 1 / speed,
          scaleX: 1,
          stagger: 0.1 / speed,
          ease: "power2.out"
        }, "-=0.4")
        .to([stats.value, actions.value], {
          duration: 0.8 / speed,
          y: 0,
          opacity: 1,
          stagger: 0.2 / speed,
          ease: "power2.out"
        }, "-=0.6")

      // 背景形状动画
      initBackgroundAnimations()
      
      // 浮动点动画
      initFloatingDotsAnimation()
      
      // 数字计数动画
      initCounterAnimations()
    }

    // 背景形状动画
    const initBackgroundAnimations = () => {
      const rotationSpeed = 20 / props.animationSpeed

      gsap.to(shape1.value, {
        duration: rotationSpeed,
        rotation: 360,
        repeat: -1,
        ease: "none"
      })

      gsap.to(shape2.value, {
        duration: rotationSpeed * 0.75,
        rotation: -360,
        repeat: -1,
        ease: "none"
      })

      gsap.to(shape3.value, {
        duration: rotationSpeed * 1.25,
        rotation: 360,
        repeat: -1,
        ease: "none"
      })
    }

    // 浮动点动画
    const initFloatingDotsAnimation = () => {
      nextTick(() => {
        dotRefs.value.forEach((dot, index) => {
          if (dot) {
            gsap.to(dot, {
              duration: (3 + Math.random() * 2) / props.animationSpeed,
              y: "-=20",
              x: "+=10",
              opacity: 0.3,
              repeat: -1,
              yoyo: true,
              ease: "sine.inOut",
              delay: index * 0.1
            })
          }
        })
      })
    }

    // 数字计数动画
    const initCounterAnimations = () => {
      statNumberRefs.value.forEach((numberEl, index) => {
        if (numberEl && props.profileData.stats[index]) {
          const finalNumber = props.profileData.stats[index].number
          const numericValue = parseInt(finalNumber.replace(/\D/g, '')) || 0
          const suffix = finalNumber.replace(/\d/g, '')
          
          gsap.from({ value: 0 }, {
            duration: 2 / props.animationSpeed,
            value: numericValue,
            onUpdate: function() {
              if (numberEl) {
                numberEl.textContent = Math.round(this.targets()[0].value) + suffix
              }
            },
            delay: 1.5 / props.animationSpeed,
            ease: "power2.out"
          })
        }
      })
    }

    // 鼠标交互效果
    const addInteractivity = () => {
      // 确保所有必要的元素都存在
      if (!poster.value || !shape1.value || !shape2.value || !shape3.value) {
        console.warn('Some elements are not ready for mouse interaction')
        return () => {} // 返回空清理函数
      }

      let isActive = true // 添加标志来追踪组件是否仍然挂载

      const handleMouseMove = (e) => {
        // 检查组件是否仍然挂载
        if (!isActive || !poster.value) return

        try {
          const rect = poster.value.getBoundingClientRect()
          const x = (e.clientX - rect.left) / rect.width - 0.5
          const y = (e.clientY - rect.top) / rect.height - 0.5

          // 使用 GSAP 的 to 方法，添加安全检查
          if (shape1.value) {
            gsap.to(shape1.value, {
              duration: 1,
              x: x * 20,
              y: y * 20,
              ease: "power2.out"
            })
          }

          if (shape2.value) {
            gsap.to(shape2.value, {
              duration: 1.5,
              x: x * -15,
              y: y * -15,
              ease: "power2.out"
            })
          }

          if (shape3.value) {
            gsap.to(shape3.value, {
              duration: 0.8,
              x: x * 25,
              y: y * 25,
              ease: "power2.out"
            })
          }
        } catch (error) {
          console.warn('Error in mouse move handler:', error)
        }
      }

      const handleMouseLeave = () => {
        if (!isActive) return

        try {
          gsap.to([shape1.value, shape2.value, shape3.value].filter(Boolean), {
            duration: 1,
            x: 0,
            y: 0,
            ease: "power2.out"
          })
        } catch (error) {
          console.warn('Error in mouse leave handler:', error)
        }
      }

      // 添加事件监听器
      poster.value.addEventListener('mousemove', handleMouseMove)
      poster.value.addEventListener('mouseleave', handleMouseLeave)

      // 返回清理函数
      return () => {
        isActive = false // 标记组件已卸载
        if (poster.value) {
          poster.value.removeEventListener('mousemove', handleMouseMove)
          poster.value.removeEventListener('mouseleave', handleMouseLeave)
        }
      }
    }

    // 在 setup 中声明清理函数引用
    let cleanupInteractivity = null

    // 操作方法
    const downloadResume = () => {
      showToastMessage('简历下载功能开发中...')
      // 这里可以实现真实的下载逻辑
      emit('contact-clicked', { action: 'download' })
    }

    const copyContact = async () => {
      try {
        const contactInfo = props.profileData.info
          .map(item => `${item.label}: ${item.value}`)
          .join('\n')
        
        await navigator.clipboard.writeText(contactInfo)
        showToastMessage('联系方式已复制到剪贴板！')
      } catch (err) {
        showToastMessage('复制失败，请手动复制')
      }
    }

    const showToastMessage = (message) => {
      toastMessage.value = message
      showToast.value = true
      
      setTimeout(() => {
        showToast.value = false
      }, 3000)
    }

    // 响应式处理
    const handleResize = () => {
      generateFloatingDots()
    }

    // 重新初始化动画（当数据变化时）
    const reinitialize = () => {
      // 清理现有动画
      gsap.killTweensOf([
        name.value, title.value, profileItemRefs.value, 
        skillsSection.value, stats.value, actions.value,
        skillBarRefs.value, dotRefs.value
      ])
      
      // 重新生成浮动点
      generateFloatingDots()
      
      // 重新初始化动画
      nextTick(() => {
        initAnimations()
      })
    }

    // 监听 props 变化
    watch(() => props.profileData, () => {
      reinitialize()
    }, { deep: true })

    watch(() => props.animationSpeed, () => {
      reinitialize()
    })

    onMounted(() => {
      generateFloatingDots()
      nextTick(() => {
        initAnimations()
        cleanupInteractivity = addInteractivity()
      })
      
      window.addEventListener('resize', handleResize)
    })

    onUnmounted(() => {
      // 清理所有事件监听器
      if (cleanupInteractivity) {
        cleanupInteractivity()
      }
      window.removeEventListener('resize', handleResize)
    })

    return {
      poster,
      header,
      name,
      title,
      profileSection,
      skillsSection,
      stats,
      actions,
      shape1,
      shape2,
      shape3,
      floatingDots,
      showToast,
      toastMessage,
      themeStyles,
      setProfileItemRef,
      setSkillBarRef,
      setStatNumberRef,
      setDotRef,
      downloadResume,
      copyContact
    }
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.poster-wrapper {
  position: relative;
  width: 100%;
  height: 100vh;
  background: v-bind('themeStyles.background');
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
  overflow: hidden;
}

.poster-container {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.background-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.shape {
  position: absolute;
  border-radius: 50%;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
  background: linear-gradient(45deg, #ff6b6b, #ffa500);
  opacity: 0.3;
}

.shape-2 {
  width: 250px;
  height: 250px;
  bottom: -80px;
  left: -80px;
  background: linear-gradient(45deg, #4ecdc4, #44a08d);
  opacity: 0.4;
}

.shape-3 {
  width: 150px;
  height: 150px;
  top: 40%;
  left: 85%;
  background: linear-gradient(45deg, #f093fb, #f5576c);
  opacity: 0.3;
}

.content {
  position: relative;
  z-index: 2;
  padding: 80px 60px 140px; /* 增加底部间距，为固定按钮留出空间 */
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #ffffff;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.name {
  font-size: 4.5rem;
  font-weight: 700;
  margin-bottom: 15px;
  color: #ffffff;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  background: linear-gradient(45deg, #ffffff, #f0f0f0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title {
  font-size: 1.6rem;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 300;
  letter-spacing: 3px;
  text-transform: uppercase;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
  flex: 1;
  margin: 40px 0;
}

.profile-section {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.profile-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(45deg, #ff6b6b, #ffa500);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
}

.label {
  font-weight: 600;
  font-size: 1.1rem;
  color: #ffffff;
  margin-bottom: 5px;
}

.value {
  color: rgba(255, 255, 255, 0.8);
  font-size: 1rem;
}

.skills-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.skills-title {
  margin-bottom: 20px;
  font-size: 1.5rem;
  color: #ffffff;
  font-weight: 600;
}

.skill-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.skill-name {
  color: #ffffff;
  font-weight: 500;
  font-size: 1.1rem;
}

.skill-bar {
  width: 250px;
  height: 10px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 5px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

.skill-progress {
  height: 100%;
  background: linear-gradient(90deg, v-bind('themeStyles.accent'), #44a08d);
  border-radius: 5px;
  transform-origin: left;
  box-shadow: 0 2px 8px rgba(68, 160, 141, 0.4);
}

/* 统计数据样式（修复后） */
.stats {
  display: flex;
  justify-content: center;
  padding: 30px 0;
  margin-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.stats-container {
  display: flex;
  flex-wrap: wrap;
  max-width: 1000px;
  gap: 30px;
  justify-content: center;
}

.stat-item {
  text-align: center;
  flex-basis: calc(33% - 20px);
  min-width: 200px;
}

.stat-number {
  font-size: 3rem;
  font-weight: 700;
  color: v-bind('themeStyles.accent');
  text-shadow: 0 2px 4px rgba(78, 205, 196, 0.3);
}

.stat-label {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8px;
  font-weight: 400;
}

/* 操作按钮样式（修复后 - 固定底部） */
.actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: transparent; /* 改为完全透明 */
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 20px;
  display: flex;
  justify-content: center;
  gap: 20px;
  z-index: 100;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1); /* 保留底部阴影增加层次感 */
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.action-btn.primary {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.action-btn.secondary {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
}

.action-btn.accent {
  background: linear-gradient(45deg, v-bind('themeStyles.accent'), #44a08d);
  color: white;
  box-shadow: 0 4px 15px rgba(78, 205, 196, 0.3);
}

.action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.btn-icon {
  font-size: 1.2rem;
}

.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.floating-dot {
  position: absolute;
  width: 8px;
  height: 8px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(255, 255, 255, 0.2);
}

/* Toast 提示 */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.9);
}

.toast-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.9);
}

.toast {
  position: fixed;
  top: 100px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.95);
  color: #333;
  padding: 15px 25px;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
}

.toast-icon {
  font-size: 1.2rem;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content {
    padding: 60px 40px 120px;
  }

  .name {
    font-size: 3.5rem;
  }

  .main-content {
    gap: 40px;
  }

  .skill-bar {
    width: 200px;
  }

  .stat-number {
    font-size: 2.5rem;
  }
}

@media (max-width: 768px) {
  .content {
    padding: 40px 30px 120px;
  }

  .main-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .name {
    font-size: 2.8rem;
  }

  .title {
    font-size: 1.2rem;
  }

  .skill-bar {
    width: 180px;
  }

  .stats-container {
    flex-direction: column;
    gap: 25px;
  }

  .stat-item {
    flex-basis: 100%;
  }

  .actions {
    flex-direction: column;
    align-items: center;
    padding: 15px;
  }

  .action-btn {
    width: 80%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .content {
    padding: 30px 20px 100px;
  }

  .name {
    font-size: 2.2rem;
  }

  .stat-number {
    font-size: 2.2rem;
  }

  .skill-bar {
    width: 150px;
  }

  .profile-item {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }

  .action-btn {
    width: 90%;
    padding: 10px 15px;
    font-size: 0.9rem;
  }
}
</style>