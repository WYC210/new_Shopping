<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">欢迎登录</h2>
      <div class="test-account">
        <p>测试账号：test</p>
        <p>测试密码：123456</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" prefix-icon="User" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" prefix-icon="Lock" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="form-footer">
        <span class="register-link" @click="goToRegister">
          还没有账号？立即注册
          <el-icon><ArrowRight /></el-icon>
        </span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const loginFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    console.log('提交登录表单:', loginForm)
    // 调用store中的login方法
    const loginResult = await userStore.login(loginForm)
    
    if (loginResult) {
      // 登录成功后跳转
      router.push('/')
    }
  } catch (error) {
    console.error('登录过程出错:', error)
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('登录失败，请稍后再试')
    }
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--primary-gradient);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;

  opacity: 0.1;
  z-index: 0;
}

.login-card {
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

.login-card::before {
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

.login-card:hover {
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

.test-account {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 25px;
  position: relative;
  overflow: hidden;
}

.test-account::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.1),
    transparent
  );
  animation: shine 2s linear infinite;
  mix-blend-mode: overlay;
}

@keyframes shine {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.test-account p {
  color: rgba(255, 255, 255, 0.9);
  margin: 5px 0;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.test-account p::before {
  content: '•';
  color: #fff;
  font-size: 18px;
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

.login-btn {
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

.login-btn::before {
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

.login-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.login-btn:hover::before {
  transform: translateX(100%);
}

.login-btn:active {
  transform: translateY(0);
}

@media (max-width: 576px) {
  .login-card {
    width: 90%;
    padding: 20px;
  }
  
  .title {
    font-size: 24px;
    margin-bottom: 20px;
  }
  
  .test-account {
    padding: 12px;
    margin-bottom: 20px;
  }
  
  .test-account p {
    font-size: 13px;
  }
  
  .login-btn {
    height: 40px;
    font-size: 15px;
  }
}

.form-footer {
  margin-top: 20px;
  text-align: center;
}

.register-link {
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

.register-link:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(5px);
}

.register-link .el-icon {
  font-size: 16px;
  transition: transform 0.3s ease;
}

.register-link:hover .el-icon {
  transform: translateX(3px);
}
</style>
