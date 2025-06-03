<template>
  <div class="profile-orders">
    <h2 class="section-title">我的订单</h2>
    
    <!-- 订单状态切换 -->
    <div class="order-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="待付款" name="unpaid"></el-tab-pane>
        <el-tab-pane label="待发货" name="unshipped"></el-tab-pane>
        <el-tab-pane label="待收货" name="shipped"></el-tab-pane>
        <el-tab-pane label="已完成" name="completed"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-loading="loading">
      <el-empty v-if="orders.length === 0" description="暂无订单"></el-empty>
      
      <div v-else class="order-items">
        <div
          v-for="order in orders"
          :key="order.id"
          class="order-item"
        >
          <div class="order-header">
            <div class="order-info">
              <span class="order-id">订单号：{{ order.orderNo }}</span>
              <span class="order-time">{{ order.createTime }}</span>
            </div>
            <div class="order-status">
              <el-tag :type="getStatusType(order.status)">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </div>
          
          <div class="order-content">
            <div
              v-for="item in order.orderItems"
              :key="item.orderItemId"
              class="order-product"
            >
              <el-image
                :src="item.productImage"
                :alt="item.productName"
                class="product-image"
              ></el-image>
              <div class="product-info">
                <div class="product-name">{{ item.productName }}</div>
                <div class="product-spec" v-if="item.spec">{{ item.spec }}</div>
                <div class="product-price">
                  <span class="price">¥{{ item.price }}</span>
                  <span class="quantity">x{{ item.quantity }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              共{{ getTotalQuantity(order.orderItems) }}件商品
              合计：<span class="total-price">¥{{ order.totalAmount }}</span>
            </div>
            
            <div class="order-actions">
              <template v-if="order.status === 'pending'">
                <el-button @click="handleCancel(order)">取消订单</el-button>
                <el-button type="primary" @click="handlePay(order)">立即付款</el-button>
              </template>
              
              <template v-if="order.status === 'shipped'">
                <el-button @click="handleViewLogistics(order)">查看物流</el-button>
                <el-button type="primary" @click="handleConfirm(order)">确认收货</el-button>
              </template>
              
              <template v-if="order.status === 'completed'">
                <el-button @click="handleViewLogistics(order)">查看物流</el-button>
                <el-button type="primary" @click="handleReview(order)">评价</el-button>
              </template>
              
              <el-button v-if="order.status === 'completed'" @click="handleDelete(order)">
                删除订单
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
          :page-sizes="[10, 20, 30, 50]"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { profileApi } from '../../api/profile'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('all')
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending':
      return 'warning'
    case 'paid':
      return 'info'
    case 'shipped':
      return 'primary'
    case 'completed':
      return 'success'
    default:
      return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending':
      return '待付款'
    case 'paid':
      return '待发货'
    case 'shipped':
      return '待收货'
    case 'completed':
      return '已完成'
    default:
      return status
  }
}

// 计算商品总数量
const getTotalQuantity = (items) => {
  if (!items || !Array.isArray(items)) return 0
  return items.reduce((total, item) => total + (item.quantity || 0), 0)
}

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    console.log('开始获取订单列表...', {
      status: activeTab.value,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    const response = await profileApi.getOrders({
      status: activeTab.value,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    console.log('订单列表响应:', response)
    if (response.code === 200) {
      orders.value = response.data
      total.value = response.data.length // 如果后端没有返回总数，暂时使用数组长度
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 切换标签
const handleTabChange = () => {
  currentPage.value = 1
  fetchOrders()
}

// 切换每页数量
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchOrders()
}

// 切换页码
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchOrders()
}

// 取消订单
const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      type: 'warning'
    })
    console.log('开始取消订单...', order.orderId)
    const response = await profileApi.cancelOrder(order.orderId)
    console.log('取消订单响应:', response)
    if (response.code === 200) {
      ElMessage.success('订单已取消')
      fetchOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 支付订单
const handlePay = (order) => {
  router.push({
    path: '/payment',
    query: {
      orderId: order.orderId
    }
  })
}

// 查看物流
const handleViewLogistics = (order) => {
  router.push({
    path: '/logistics',
    query: {
      orderId: order.orderId
    }
  })
}

// 确认收货
const handleConfirm = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '提示', {
      type: 'warning'
    })
    console.log('开始确认收货...', order.orderId)
    const response = await profileApi.confirmOrder(order.orderId)
    console.log('确认收货响应:', response)
    if (response.code === 200) {
      ElMessage.success('确认收货成功')
      fetchOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败')
    }
  }
}

// 评价订单
const handleReview = (order) => {
  router.push({
    path: '/review',
    query: {
      orderId: order.orderId
    }
  })
}

// 删除订单
const handleDelete = async (order) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？', '提示', {
      type: 'warning'
    })
    
    await userStore.deleteOrder(order.orderId)
    ElMessage.success('删除成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.profile-orders {
  padding: 20px;
}

.section-title {
  font-size: 24px;
  color: #333;
  font-weight: 500;
  margin-bottom: 30px;
}

.order-tabs {
  margin-bottom: 20px;
}

.order-list {
  min-height: 200px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-item {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.order-info {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.order-content {
  padding: 20px;
}

.order-product {
  display: flex;
  gap: 20px;
  padding: 10px 0;
}

.order-product:not(:last-child) {
  border-bottom: 1px solid #ebeef5;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}

.product-spec {
  font-size: 12px;
  color: #999;
}

.product-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: 500;
}

.quantity {
  color: #999;
  font-size: 14px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f5f7fa;
  border-top: 1px solid #ebeef5;
}

.order-total {
  color: #666;
  font-size: 14px;
}

.total-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 500;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
}

:deep(.el-tabs__item.is-active) {
  font-weight: 500;
}
</style> 