<template>
  <div class="profile-orders">
    <h2 class="section-title">我的订单</h2>
    
    <!-- 订单状态切换 -->
    <div class="order-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="全部" name="all"></el-tab-pane>
        <el-tab-pane label="待付款" name="pending"></el-tab-pane>
        <el-tab-pane label="已付款" name="paid"></el-tab-pane>
        <el-tab-pane label="已取消" name="cancelled"></el-tab-pane>
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
              <el-tag :type="getStatusType(order.status) || 'info'">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </div>
          
          <div class="order-content">
            <div
              v-if="order.orderItems && order.orderItems.length > 0"
              v-for="item in order.orderItems"
              :key="item.orderItemId"
              class="order-product"
            >
              <el-image
                :src="item.productImage || '/images/2.jpg'"
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
            <!-- 当没有订单项时显示简化信息 -->
            <div v-else class="order-product">
              <el-image
                src="/images/2.jpg"
                alt="商品"
                class="product-image"
              ></el-image>
              <div class="product-info">
                <div class="product-name">商品 x{{ order.itemCount || 1 }}</div>
                <div class="product-price">
                  <span class="price">¥{{ order.totalAmount }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              共{{ order.orderItems && order.orderItems.length > 0 ? getTotalQuantity(order.orderItems) : (order.itemCount || 1) }}件商品
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
              
              <el-button v-if="order.status === 'cancelled' || order.status === 'completed' || order.status === 'paid'" @click="handleDelete(order)">
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
import { ref, onMounted, defineEmits } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { profileApi } from '../../api/profile'

const emit = defineEmits(['order-paid'])

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
      return 'primary'
    case 'cancelled':
      return 'info'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending':
      return '待付款'
    case 'paid':
      return '已付款'
    case 'cancelled':
      return '已取消'
    default:
      return '未知状态'
  }
}

// 计算商品总数量
const getTotalQuantity = (items) => {
  if (!items || !Array.isArray(items) || items.length === 0) {
    return 1; // 默认至少显示1件商品
  }
  return items.reduce((total, item) => total + (item.quantity || 0), 0)
}

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const status = activeTab.value === 'all' ? '' : activeTab.value
    console.log('开始获取订单列表...', {
      status,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    const response = await profileApi.getOrders({
      status,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    console.log('订单列表响应:', response)
    if (response) {
      // 适配新的API响应结构
      if (response.data && Array.isArray(response.data)) {
        orders.value = response.data.map(order => ({
          orderId: order.orderId,
          orderNo: order.orderId.toString(), // 使用orderId作为订单号
          status: order.status,
          totalAmount: order.totalAmount,
          createTime: new Date(order.createdAt).toLocaleString(),
          orderItems: order.orderItems || [] // 如果没有orderItems，使用空数组
        }));
        total.value = response.total || 0;
      } else if (response.list && Array.isArray(response.list)) {
        // 处理直接传入的数据结构
        orders.value = response.list.map(order => ({
          orderId: order.orderId,
          orderNo: order.orderId.toString(), // 使用orderId作为订单号
          status: order.status,
          totalAmount: order.totalAmount,
          createTime: new Date(order.createdAt).toLocaleString(),
          orderItems: order.orderItems || [{
            orderItemId: `item-${order.orderId}-1`,
            productName: `商品 x${order.itemCount || 1}`,
            productImage: '/images/cloud.jpeg',
            price: order.totalAmount / (order.itemCount || 1),
            quantity: order.itemCount || 1
          }]
        }));
        total.value = response.total || 0;
      }
      
      // 如果当前页没有数据但总数大于0，可能是删除了最后一页的最后一条数据
      // 此时自动跳转到前一页
      if (orders.value.length === 0 && total.value > 0 && currentPage.value > 1) {
        currentPage.value--
        fetchOrders()
        return
      }
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 切换标签
const handleTabChange = (tab) => {
  console.log('切换标签:', tab.props.name)
  activeTab.value = tab.props.name
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

// 在支付成功后返回此页面时检查并更新用户余额
const checkPaymentSuccess = () => {
  const paymentSuccess = localStorage.getItem('payment_success')
  if (paymentSuccess) {
    // 清除标记
    localStorage.removeItem('payment_success')
    // 触发父组件的刷新方法
    emit('order-paid')
    // 刷新订单列表
    fetchOrders()
  }
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

// 初始化加载
onMounted(() => {
  console.log('组件挂载，初始状态:', activeTab.value)
  fetchOrders()
  // 检查是否有支付成功的标记
  checkPaymentSuccess()
})
</script>

<style scoped>
.profile-orders {
  padding: 20px;
}

.section-title {
  font-size: 24px;
  color: #fff;
  font-weight: 500;
  margin-bottom: 30px;
}

.order-tabs {
  margin-bottom: 20px;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: rgba(255, 255, 255, 0.1);
}

:deep(.el-tabs__item) {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
}

:deep(.el-tabs__item.is-active) {
  font-weight: 500;
  color: #fff;
}

:deep(.el-tabs__active-bar) {
  background-color: rgba(255, 255, 255, 0.6);
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
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.order-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.order-info {
  display: flex;
  gap: 20px;
  color: rgba(255, 255, 255, 0.7);
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
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 6px;
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
  color: #fff;
  margin-bottom: 8px;
}

.product-spec {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.product-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  color: #ff6b6b;
  font-size: 16px;
  font-weight: 500;
}

.quantity {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.05);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.order-total {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.total-price {
  color: #ff6b6b;
  font-size: 18px;
  font-weight: 500;
}

.order-actions {
  display: flex;
  gap: 10px;
}

:deep(.el-button) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

:deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

:deep(.el-button--primary) {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

:deep(.el-button--primary:hover) {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.4);
}

.pagination {
  margin-top: 30px;
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
</style> 