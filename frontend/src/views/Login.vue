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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

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
    
    // 调用store中的login方法,它会调用authApi.login
    await userStore.login(loginForm)
    
    // 登录成功后直接跳转，不需要额外提示
    router.push('/')
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f4f6f9;
}

.login-card {
  width: 400px;
  padding: 30px;
}

.title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
}

.test-account {
  text-align: center;
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 4px;
  color: #67c23a;
  font-size: 14px;
}

.test-account p {
  margin: 5px 0;
}
</style>
