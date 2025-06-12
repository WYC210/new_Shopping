import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'
import apiClient from './api/client'
import { ElMessage } from 'element-plus'

const app = createApp(App)

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  console.error('全局错误:', err)
  console.error('错误组件:', vm)
  console.error('错误信息:', info)
  
  // 检查是否是常见错误类型
  if (err instanceof TypeError && err.message.includes('toFixed')) {
    console.warn('检测到格式化数字错误，可能是数据结构问题')
  }
  
  // 限制错误消息显示频率，避免消息轰炸
  const now = Date.now()
  const lastErrorTime = app.config.globalProperties.$lastErrorTime || 0
  if (now - lastErrorTime > 5000) { // 5秒内不重复显示错误
    ElMessage.error('应用发生错误，请刷新页面')
    app.config.globalProperties.$lastErrorTime = now
  }
}

// Register all Element Plus icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 初始化完成后，初始化用户状态
// 需要在pinia初始化之后执行
import { useUserStore } from './stores/user'
router.isReady().then(() => {
  const userStore = useUserStore()
  userStore.initFromStorage()
})

app.mount('#app')
