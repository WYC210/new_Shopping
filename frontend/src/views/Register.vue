<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 class="title">注册账号</h2>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" prefix-icon="User" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" prefix-icon="Message" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" prefix-icon="Phone" placeholder="请输入手机号" />
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Message, Phone, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const registerFormRef = ref(null)

const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword) {
      registerFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (!value) {
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
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
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
    
    console.log('开始注册，表单数据:', {
      username: registerForm.username,
      email: registerForm.email,
      phone: registerForm.phone
    });
    
    // 调用store中的register方法,它会调用authApi.register
    await userStore.register({
      username: registerForm.username,
      email: registerForm.email,
      phone: registerForm.phone,
      password: registerForm.password
    })
    
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error);
    
    // 处理特定错误
    if (error.message && error.message.includes('用户名已存在')) {
      ElMessage.error('用户名已被注册，请更换用户名');
      // 聚焦用户名输入框
      registerFormRef.value.fields.find(f => f.prop === 'username')?.focus();
    } else if (error.message && error.message.includes('邮箱已存在')) {
      ElMessage.error('邮箱已被注册，请更换邮箱');
      // 聚焦邮箱输入框
      registerFormRef.value.fields.find(f => f.prop === 'email')?.focus();
    } else if (error.message && error.message.includes('手机号已存在')) {
      ElMessage.error('手机号已被注册，请更换手机号');
      // 聚焦手机号输入框
      registerFormRef.value.fields.find(f => f.prop === 'phone')?.focus();
    } else {
      // 其他错误
      ElMessage.error(error.message || '注册失败，请稍后重试');
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #eef1f6;
}

.register-card {
  width: 500px;
  padding: 30px;
}

.title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 20px;
}

.register-btn {
  width: 100%;
}
</style>
