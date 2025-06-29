<template>
  <div class="profile-info">
    <h2 class="section-title">账号信息</h2>
    
    <div class="balance-card">
      <div class="balance-title">账户余额</div>
      <div class="balance-amount">¥{{ userStore.balance.toFixed(2) }}</div>
    </div>
    
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="info-form"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" disabled></el-input>
      </el-form-item>
      
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" :disabled="!isEditing"></el-input>
      </el-form-item>
      
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" :disabled="!isEditing"></el-input>
      </el-form-item>
      
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="form.realName" :disabled="!isEditing"></el-input>
      </el-form-item>
      
      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="form.gender" :disabled="!isEditing">
          <el-radio :label="1">男</el-radio>
          <el-radio :label="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="生日" prop="birthday">
        <el-date-picker
          v-model="form.birthday"
          type="date"
          placeholder="选择日期"
          :disabled="!isEditing"
          value-format="YYYY-MM-DD"
        ></el-date-picker>
      </el-form-item>
      
      <el-form-item>
        <template v-if="!isEditing">
          <el-button type="primary" @click="startEdit">编辑信息</el-button>
        </template>
        <template v-else>
          <el-button type="primary" @click="handleSubmit" :loading="loading">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </template>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { profileApi } from '@/api/profile'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const formRef = ref(null)
const form = ref({
  username: '',
  email: '',
  phone: '',
  realName: '',
  gender: '',
  birthday: ''
})

const loading = ref(false)
const isEditing = ref(false)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const fetchUserInfo = async () => {
  loading.value = true
  try {
    console.log('开始获取用户信息...')
    const response = await profileApi.getUserInfo()
    console.log('用户信息响应:', response)
    if (response.code === 200) {
      form.value = response.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    console.log('开始保存用户信息...', form.value)
    const response = await profileApi.updateUserInfo(form.value)
    console.log('保存用户信息响应:', response)
    if (response.code === 200) {
      ElMessage.success('保存成功')
      isEditing.value = false
    }
  } catch (error) {
    console.error('保存用户信息失败:', error)
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

const startEdit = () => {
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  fetchUserInfo()
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile-info {
  padding: 20px;
  color: #fff;
}

.section-title {
  margin-bottom: 30px;
  font-size: 24px;
  color: #fff;
  font-weight: 500;
}

.balance-card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.balance-title {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 10px;
}

.balance-amount {
  font-size: 28px;
  font-weight: 600;
  color: #67c23a;
}

.info-form {
  max-width: 600px;
}

:deep(.el-form-item__label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  box-shadow: none !important;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

:deep(.el-input__inner) {
  color: #fff;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.7);
  border-color: rgba(255, 255, 255, 0.1);
}

:deep(.el-input.is-disabled .el-input__inner) {
  color: rgba(255, 255, 255, 0.7);
}

:deep(.el-radio__label) {
  color: rgba(255, 255, 255, 0.9);
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

:deep(.el-button) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

:deep(.el-button--primary) {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

:deep(.el-button--primary:hover) {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
}
</style> 