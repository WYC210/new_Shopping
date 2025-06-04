<template>
  <page-transition>
    <div class="register-container">
      <el-card class="register-card">
        <h2 class="title">欢迎注册</h2>
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" prefix-icon="User" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="registerForm.password" prefix-icon="Lock" type="password" placeholder="请输入密码" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" prefix-icon="Lock" type="password" placeholder="请再次输入密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleRegister" class="register-btn">注册</el-button>
          </el-form-item>
        </el-form>
        <div class="form-footer">
          <span class="login-link" @click="goToLogin">
            已有账号？立即登录
            <el-icon><ArrowRight /></el-icon>
          </span>
        </div>
      </el-card>
    </div>
  </page-transition>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowRight } from '@element-plus/icons-vue'
import PageTransition from '../components/PageTransition.vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const registerFormRef = ref(null)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    await userStore.register({
      username: registerForm.username,
      password: registerForm.password
    })
    
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--primary-gradient);
  position: relative;
  overflow: hidden;
}

.register-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('/src/assets/pattern.png') repeat;
  opacity: 0.1;
  z-index: 0;
}

.register-card {
  width: 400px;
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  border-radius: 20px !important;
  padding: 30px;
  position: relative;
  z-index: 1;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.register-card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(255, 255, 255, 0.1),
    transparent
  );
  transform: rotate(45deg);
  animation: scan 3s linear infinite;
  mix-blend-mode: overlay;
}

@keyframes scan {
  0% {
    transform: translateY(-100%) rotate(45deg);
  }
  100% {
    transform: translateY(100%) rotate(45deg);
  }
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3) !important;
}

.title {
  color: #fff;
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 30px;
  position: relative;
}

.title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 50px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #fff, transparent);
  animation: glow 2s ease-in-out infinite;
}

@keyframes glow {
  0%, 100% {
    opacity: 0.5;
    width: 50px;
  }
  50% {
    opacity: 1;
    width: 100px;
  }
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.9) !important;
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: rgba(255, 255, 255, 0.4) !important;
  transform: translateY(-2px);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: rgba(255, 255, 255, 0.6) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.2) !important;
}

:deep(.el-input__inner) {
  color: #fff !important;
}

:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

:deep(.el-input__prefix-inner) {
  color: rgba(255, 255, 255, 0.7);
}

.register-btn {
  width: 100%;
  height: 45px;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: #fff !important;
  font-size: 16px;
  font-weight: 500;
  border-radius: 12px !important;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.register-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    45deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.register-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.register-btn:hover::before {
  transform: translateX(100%);
}

.register-btn:active {
  transform: translateY(0);
}

.form-footer {
  margin-top: 20px;
  text-align: center;
}

.login-link {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s ease;
  padding: 5px 10px;
  border-radius: 8px;
}

.login-link:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(5px);
}

.login-link .el-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.login-link:hover .el-icon {
  transform: translateX(3px);
}

@media (max-width: 576px) {
  .register-card {
    width: 90%;
    padding: 20px;
  }
  
  .title {
    font-size: 24px;
    margin-bottom: 20px;
  }
  
  .register-btn {
    height: 40px;
    font-size: 15px;
  }
}
</style>
