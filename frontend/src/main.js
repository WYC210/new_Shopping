import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'
import apiClient from './api/client'

const app = createApp(App)

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
