<template>
  <div class="profile-coupons">
    <h2 class="section-title">我的优惠券</h2>
    
    <!-- 状态切换 -->
    <div class="coupon-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="未使用" name="unused">
          <div class="coupon-count">共 {{ unusedCount }} 张</div>
        </el-tab-pane>
        <el-tab-pane label="已使用" name="used">
          <div class="coupon-count">共 {{ usedCount }} 张</div>
        </el-tab-pane>
        <el-tab-pane label="已过期" name="expired">
          <div class="coupon-count">共 {{ expiredCount }} 张</div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 优惠券列表 -->
    <div class="coupon-list" v-loading="loading">
      <el-empty v-if="coupons.length === 0" :description="emptyText"></el-empty>
      
      <div v-else class="coupon-items">
        <div
          v-for="coupon in coupons"
          :key="coupon.userCouponId || coupon.id"
          class="coupon-item"
          :class="{
            'is-used': coupon.status === 'used',
            'is-expired': coupon.status === 'expired'
          }"
        >
          <div class="coupon-content">
            <div class="coupon-left">
              <div class="coupon-amount">
                <template v-if="coupon.couponType === 'fixed'">
                  <span class="currency">¥</span>
                  <span class="number">{{ coupon.discountAmount }}</span>
                </template>
                <template v-else-if="coupon.couponType === 'percentage'">
                  <span class="number">{{ (coupon.discountPercentage / 10).toFixed(1) }}</span>
                  <span class="unit">折</span>
                </template>
                <template v-else-if="coupon.couponType === 'shipping'">
                  <span class="number">免运费</span>
                </template>
                <template v-else>
                  <span class="number">其他</span>
                </template>
              </div>
              <div class="coupon-condition">
                <template v-if="coupon.couponType === 'shipping' && coupon.threshold">
                  满{{ coupon.threshold }}元包邮
                </template>
                <template v-else-if="coupon.threshold">
                  满{{ coupon.threshold }}元可用
                </template>
                <template v-else>
                  无门槛
                </template>
              </div>
              <div class="coupon-scope" v-if="coupon.applicableScope && coupon.applicableScope !== 'all'">
                适用范围：{{ coupon.applicableScope === 'category' ? '指定品类' : coupon.applicableScope }}
              </div>
            </div>
            
            <div class="coupon-right">
              <div class="coupon-info">
                <div class="coupon-name">{{ coupon.couponName || coupon.name }}</div>
                <div class="coupon-time">
                  {{ coupon.startTime || coupon.createdAt }}<template v-if="coupon.endTime"> - {{ coupon.endTime }}</template>
                </div>
              </div>
              
              <div class="coupon-status">
                <el-tag
                  :type="getStatusType(coupon.status)"
                  size="small"
                >
                  {{ getStatusText(coupon.status) }}
                </el-tag>
              </div>
            </div>
          </div>
          
          <div class="coupon-footer" v-if="coupon.status === 'unused'">
            <el-button
              type="primary"
              link
              @click="handleUse(coupon)"
            >
              立即使用
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import { profileApi } from '../../api/profile'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('unused')
const coupons = ref([])

// 计算各状态优惠券数量
const unusedCount = computed(() => coupons.value.filter(c => c.status === 'unused').length)
const usedCount = computed(() => coupons.value.filter(c => c.status === 'used').length)
const expiredCount = computed(() => coupons.value.filter(c => c.status === 'expired').length)

// 空状态文本
const emptyText = computed(() => {
  switch (activeTab.value) {
    case 'unused':
      return '暂无可用优惠券'
    case 'used':
      return '暂无已使用优惠券'
    case 'expired':
      return '暂无已过期优惠券'
    default:
      return '暂无优惠券'
  }
})

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'unused':
      return 'success'
    case 'used':
      return 'info'
    case 'expired':
      return 'danger'
    default:
      return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'unused':
      return '未使用'
    case 'used':
      return '已使用'
    case 'expired':
      return '已过期'
    default:
      return ''
  }
}

// 获取优惠券列表
const fetchCoupons = async () => {
  loading.value = true
  try {
    console.log('开始获取优惠券列表...')
    const response = await profileApi.getCoupons()
    console.log('优惠券列表响应:', response)
    if (response.code === 200) {
      coupons.value = response.data
    }
  } catch (error) {
    console.error('获取优惠券列表失败:', error)
    ElMessage.error('获取优惠券列表失败')
  } finally {
    loading.value = false
  }
}

// 切换标签
const handleTabChange = () => {
  // 可以在这里添加筛选逻辑
}

// 使用优惠券
const handleUse = (coupon) => {
  // 跳转到商品列表页面
  router.push({
    path: '/products',
    query: {
      couponId: coupon.id
    }
  })
}

onMounted(() => {
  fetchCoupons()
})
</script>

<style scoped>
.profile-coupons {
  padding: 20px;
}

.section-title {
  font-size: 24px;
  color: #fff;
  font-weight: 500;
  margin-bottom: 30px;
}

.coupon-tabs {
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

.coupon-count {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  margin-top: 8px;
}

.coupon-list {
  min-height: 200px;
}

.coupon-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.coupon-item {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.coupon-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.coupon-item.is-used,
.coupon-item.is-expired {
  opacity: 0.7;
}

.coupon-content {
  display: flex;
  padding: 20px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.2) 0%, rgba(54, 209, 220, 0.2) 100%);
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.coupon-item.is-used .coupon-content,
.coupon-item.is-expired .coupon-content {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.2) 0%, rgba(96, 98, 102, 0.2) 100%);
}

.coupon-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-right: 20px;
  border-right: 1px dashed rgba(255, 255, 255, 0.3);
  min-width: 120px;
}

.coupon-amount {
  display: flex;
  align-items: baseline;
}

.coupon-amount .currency {
  font-size: 16px;
  margin-right: 2px;
}

.coupon-amount .number {
  font-size: 32px;
  font-weight: bold;
}

.coupon-condition {
  font-size: 12px;
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.7);
}

.coupon-right {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-left: 20px;
}

.coupon-info {
  flex: 1;
}

.coupon-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #fff;
}

.coupon-time,
.coupon-scope {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 4px;
}

.coupon-footer {
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.05);
  text-align: right;
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

:deep(.el-tag) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

:deep(.el-tag--success) {
  background: rgba(103, 194, 58, 0.2);
  border-color: rgba(103, 194, 58, 0.3);
}

:deep(.el-tag--info) {
  background: rgba(144, 147, 153, 0.2);
  border-color: rgba(144, 147, 153, 0.3);
}

:deep(.el-tag--danger) {
  background: rgba(245, 108, 108, 0.2);
  border-color: rgba(245, 108, 108, 0.3);
}
</style> 