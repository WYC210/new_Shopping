<template>
  <div class="profile-info">
    <h2 class="section-title">账号信息</h2>
    
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
}

.section-title {
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
  font-weight: 500;
}

.info-form {
  max-width: 600px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input.is-disabled .el-input__inner) {
  background-color: #f5f7fa;
  color: #606266;
}
</style> 