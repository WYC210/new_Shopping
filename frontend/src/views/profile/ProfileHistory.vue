<template>
  <div class="profile-history">
    <h2 class="section-title">浏览记录</h2>
    
    <!-- 浏览记录列表 -->
    <div class="history-list" v-loading="loading">
      <el-empty v-if="history.length === 0" description="暂无浏览记录"></el-empty>
      
      <div v-else class="history-grid">
        <div
          v-for="item in history"
          :key="item.recordId"
          class="history-item"
        >
          <div class="history-content">
            <el-image
              :src="item.productImage"
              :alt="item.productName"
              class="product-image"
              @click="handleViewProduct(item)"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            
            <div class="product-info">
              <div class="product-name" @click="handleViewProduct(item)">
                {{ item.productName }}
              </div>
              <div class="view-time">
                浏览时间：{{ formatTime(item.viewedAt) }}
              </div>
            </div>
            
            <div class="history-actions">
              <el-button
                type="primary"
                size="small"
                @click="handleAddToCart(item)"
              >
                加入购物车
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleRemove(item)"
              >
                删除记录
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useCartStore } from '../../stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { profileApi } from '../../api/profile'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const loading = ref(false)
const history = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 获取浏览记录
const fetchHistory = async () => {
  loading.value = true
  try {
    const response = await profileApi.getBrowsingHistory()
    if (response.code === 200) {
      history.value = response.data
      total.value = response.data.length
    }
  } catch (error) {
    console.error('获取浏览记录失败:', error)
    ElMessage.error('获取浏览记录失败')
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

// 切换每页数量
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchHistory()
}

// 切换页码
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchHistory()
}

// 查看商品详情
const handleViewProduct = (item) => {
  router.push(`/product/${item.productId}`)
}

// 加入购物车
const handleAddToCart = async (item) => {
  try {
    await cartStore.addToCart({
      productId: item.productId,
      quantity: 1
    })
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error('加入购物车失败')
  }
}

// 删除记录
const handleRemove = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除该浏览记录吗？', '提示', {
      type: 'warning'
    })
    
    await profileApi.deleteBrowsingHistory(item.recordId)
    ElMessage.success('已删除浏览记录')
    fetchHistory()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除浏览记录失败')
    }
  }
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.profile-history {
  padding: 20px;
}

.section-title {
  font-size: 24px;
  color: #fff;
  font-weight: 500;
  margin-bottom: 30px;
}

.history-list {
  min-height: 200px;
}

.history-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.history-item {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.history-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.history-content {
  display: flex;
  flex-direction: column;
}

.product-image {
  width: 100%;
  height: 240px;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.5s ease;
}

.history-item:hover .product-image {
  transform: scale(1.05);
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.5);
  font-size: 24px;
}

.product-info {
  padding: 15px;
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
}

.product-name {
  font-size: 14px;
  color: #fff;
  margin-bottom: 10px;
  cursor: pointer;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  transition: color 0.3s ease;
}

.product-name:hover {
  color: rgba(255, 255, 255, 0.8);
}

.view-time {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
}

.history-actions {
  padding: 15px;
  display: flex;
  gap: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.03);
}

.pagination {
  display: flex;
  justify-content: center;
}

:deep(.el-pagination) {
  --el-pagination-bg-color: rgba(255, 255, 255, 0.1);
  --el-pagination-hover-color: #fff;
  --el-pagination-button-color: rgba(255, 255, 255, 0.8);
  --el-pagination-button-bg-color: rgba(255, 255, 255, 0.1);
  --el-pagination-button-disabled-color: rgba(255, 255, 255, 0.4);
  --el-pagination-button-disabled-bg-color: rgba(255, 255, 255, 0.05);
}

:deep(.el-button) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.9);
}

:deep(.el-button--primary) {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
}

:deep(.el-button--primary:hover) {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
}

:deep(.el-button--danger) {
  background: rgba(245, 108, 108, 0.2);
  border: 1px solid rgba(245, 108, 108, 0.3);
  color: #fff;
}

:deep(.el-button--danger:hover) {
  background: rgba(245, 108, 108, 0.3);
  border-color: rgba(245, 108, 108, 0.4);
}
</style> 