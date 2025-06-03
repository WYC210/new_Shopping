<template>
  <div class="profile-address">
    <div class="section-header">
      <h2 class="section-title">收货地址</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增地址
      </el-button>
    </div>

    <!-- 地址列表 -->
    <div class="address-list" v-loading="loading">
      <el-empty v-if="addresses.length === 0" description="暂无收货地址"></el-empty>
      
      <div v-else class="address-items">
        <div
          v-for="address in addresses"
          :key="address.id"
          class="address-item"
          :class="{ 'is-default': address.isDefault }"
        >
          <div class="address-content">
            <div class="address-info">
              <div class="address-header">
                <span class="name">{{ address.name }}</span>
                <span class="phone">{{ address.phone }}</span>
                <el-tag v-if="address.isDefault" type="success" size="small">默认</el-tag>
              </div>
              <div class="address-detail">
                {{ address.province }} {{ address.city }} {{ address.district }}
                {{ address.detail }}
              </div>
            </div>
            <div class="address-actions">
              <el-button
                link
                type="primary"
                @click="handleEdit(address)"
              >
                编辑
              </el-button>
              <el-button
                link
                type="primary"
                @click="handleSetDefault(address)"
                v-if="!address.isDefault"
              >
                设为默认
              </el-button>
              <el-button
                link
                type="danger"
                @click="handleDelete(address)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 地址表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑地址' : '新增地址'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="收货人" prop="name">
          <el-input v-model="form.name" placeholder="请输入收货人姓名"></el-input>
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="form.region"
            :options="regionOptions"
            placeholder="请选择所在地区"
          ></el-cascader>
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detail">
          <el-input
            v-model="form.detail"
            type="textarea"
            :rows="2"
            placeholder="请输入详细地址"
          ></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="form.isDefault">设为默认收货地址</el-checkbox>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { profileApi } from '../../api/profile'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const addresses = ref([])

// 表单数据
const form = reactive({
  id: null,
  name: '',
  phone: '',
  region: [],
  detail: '',
  isDefault: false
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 获取地址列表
const fetchAddresses = async () => {
  loading.value = true
  try {
    console.log('开始获取地址列表...')
    const response = await profileApi.getAddresses()
    console.log('地址列表响应:', response)
    if (response.code === 200) {
      addresses.value = response.data
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
    ElMessage.error('获取地址列表失败')
  } finally {
    loading.value = false
  }
}

// 新增地址
const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    phone: '',
    region: [],
    detail: '',
    isDefault: false
  })
  dialogVisible.value = true
}

// 编辑地址
const handleEdit = (address) => {
  isEdit.value = true
  Object.assign(form, {
    id: address.id,
    name: address.name,
    phone: address.phone,
    region: [address.province, address.city, address.district],
    detail: address.detail,
    isDefault: address.isDefault
  })
  dialogVisible.value = true
}

// 删除地址
const handleDelete = async (address) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      type: 'warning'
    })
    
    await userStore.deleteAddress(address.id)
    ElMessage.success('删除成功')
    fetchAddresses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 设置默认地址
const handleSetDefault = async (address) => {
  try {
    await userStore.setDefaultAddress(address.id)
    ElMessage.success('设置成功')
    fetchAddresses()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const [province, city, district] = form.region
    const addressData = {
      name: form.name,
      phone: form.phone,
      province,
      city,
      district,
      detail: form.detail,
      isDefault: form.isDefault
    }
    
    if (isEdit.value) {
      await userStore.updateAddress(form.id, addressData)
      ElMessage.success('修改成功')
    } else {
      await userStore.addAddress(addressData)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    fetchAddresses()
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchAddresses()
})
</script>

<style scoped>
.profile-address {
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.section-title {
  font-size: 24px;
  color: #333;
  font-weight: 500;
  margin: 0;
}

.address-list {
  min-height: 200px;
}

.address-items {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.address-item {
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s;
}

.address-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.address-item.is-default {
  border-color: #67c23a;
}

.address-content {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.address-info {
  flex: 1;
}

.address-header {
  margin-bottom: 10px;
}

.address-header .name {
  font-size: 16px;
  font-weight: 500;
  margin-right: 10px;
}

.address-header .phone {
  color: #666;
  margin-right: 10px;
}

.address-detail {
  color: #666;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

:deep(.el-dialog__body) {
  padding-top: 20px;
}
</style> 