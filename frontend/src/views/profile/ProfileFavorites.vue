<template>
  <div class="profile-favorites">
    <h2 class="section-title">我的收藏</h2>
    
    <!-- 收藏列表 -->
    <div class="favorites-list" v-loading="loading">
      <el-empty v-if="favorites.length === 0" description="暂无收藏商品"></el-empty>
      
      <div v-else class="favorites-grid">
        <div
          v-for="item in favorites"
          :key="item.id"
          class="favorite-item"
        >
          <div class="favorite-content">
            <el-image
              :src="item.image"
              :alt="item.name"
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
                {{ item.name }}
              </div>
              <div class="product-price">
                <span class="price">¥{{ item.price }}</span>
                <span class="original-price" v-if="item.originalPrice">
                  ¥{{ item.originalPrice }}
                </span>
              </div>
            </div>
            
            <div class="favorite-actions">
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
                取消收藏
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

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const loading = ref(false)
const favorites = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 获取收藏列表
const fetchFavorites = async () => {
  loading.value = true
  try {
    console.log('开始获取收藏列表...')
    const response = await profileApi.getFavorites()
    console.log('收藏列表响应:', response)
    if (response.code === 200) {
      favorites.value = response.data
    }
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 切换每页数量
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchFavorites()
}

// 切换页码
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchFavorites()
}

// 查看商品详情
const handleViewProduct = (item) => {
  router.push(`/product/${item.id}`)
}

// 加入购物车
const handleAddToCart = async (item) => {
  try {
    await cartStore.addToCart({
      productId: item.id,
      quantity: 1
    })
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error('加入购物车失败')
  }
}

// 取消收藏
const handleRemove = async (item) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏该商品吗？', '提示', {
      type: 'warning'
    })
    
    await userStore.removeFavorite(item.id)
    ElMessage.success('已取消收藏')
    fetchFavorites()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消收藏失败')
    }
  }
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
.profile-favorites {
  padding: 20px;
}

.section-title {
  font-size: 24px;
  color: #333;
  font-weight: 500;
  margin-bottom: 30px;
}

.favorites-list {
  min-height: 200px;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.favorite-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.favorite-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.favorite-content {
  display: flex;
  flex-direction: column;
}

.product-image {
  width: 100%;
  height: 240px;
  object-fit: cover;
  cursor: pointer;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.product-info {
  padding: 15px;
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 10px;
  cursor: pointer;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.product-name:hover {
  color: #409eff;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 500;
}

.original-price {
  color: #999;
  font-size: 14px;
  text-decoration: line-through;
}

.favorite-actions {
  padding: 15px;
  display: flex;
  gap: 10px;
  border-top: 1px solid #ebeef5;
}

.pagination {
  display: flex;
  justify-content: center;
}
</style> 