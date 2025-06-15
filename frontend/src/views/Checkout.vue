<template>
  <div class="checkout-container content-container" v-loading="loading">
    <div class="checkout-content">
      <!-- 收货地址 -->
      <div class="section address-section">
        <h2 class="section-title">收货地址</h2>
        <div class="address-list" v-if="addresses.length">
          <div
            v-for="address in addresses"
            :key="address.addressId"
            class="address-item"
            :class="{ active: selectedAddress?.addressId === address.addressId }"
            @click="selectAddress(address)"
          >
            <div class="address-info">
              <div class="contact">
                <span class="name">{{ address.receiverName }}</span>
                <span class="phone">{{ address.receiverPhone }}</span>
                <el-tag v-if="address.isDefault === 1" size="small" type="danger">默认</el-tag>
              </div>
              <div class="address-detail">
                {{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}
              </div>
            </div>
            <div class="address-actions">
              <el-button link type="primary" @click.stop="editAddress(address)">编辑</el-button>
              <el-button link type="danger" @click.stop="deleteAddress(address)">删除</el-button>
            </div>
          </div>
          <div class="add-address" @click="showAddressDialog = true">
            <el-icon><Plus /></el-icon>
            <span>添加新地址</span>
          </div>
        </div>
        <div class="no-address" v-else>
          <el-empty description="暂无收货地址">
            <el-button type="primary" @click="showAddressDialog = true">添加地址</el-button>
          </el-empty>
        </div>
      </div>

      <!-- 商品信息 -->
      <div class="section order-section">
        <h2 class="section-title">商品信息</h2>
        <div class="order-items">
          <div v-for="item in orderItems" :key="item.id" class="order-item">
            <el-image :src="item.image" class="item-image" />
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-specs" v-if="item.specs">
                {{ Object.values(item.specs).join('，') }}
              </div>
            </div>
            <div class="item-price">¥{{ item.price }}</div>
            <div class="item-quantity">x{{ item.quantity }}</div>
            <div class="item-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
        </div>
      </div>

      <!-- 优惠券 -->
      <div class="section coupon-section">
        <h2 class="section-title">优惠券</h2>
        <div class="coupon-selector" @click="showCouponDialog = true">
          <span class="label">选择优惠券</span>
          <div class="selected-coupon" v-if="selectedCoupon">
            <el-tag type="danger" effect="dark">
              {{ selectedCoupon.couponName }}（-¥{{ selectedCoupon.value.toFixed(2) }}）
            </el-tag>
          </div>
          <div class="no-coupon" v-else>
            <span class="placeholder">请选择优惠券</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 支付方式 -->
      <div class="section payment-section">
        <h2 class="section-title">支付方式</h2>
        <div class="payment-methods">
          <div
            v-for="method in paymentMethods"
            :key="method.id"
            class="payment-method"
            :class="{ active: selectedPayment === method.id }"
            @click="selectedPayment = method.id"
          >
            <el-icon :size="24"><component :is="method.icon" /></el-icon>
            <span>{{ method.name }}</span>
          </div>
        </div>
      </div>

      <!-- 订单金额 -->
      <div class="section amount-section">
        <div class="amount-item">
          <span>商品金额</span>
          <span>¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <div class="amount-item">
          <span>运费</span>
          <span>¥{{ shipping.toFixed(2) }}</span>
        </div>
        <div class="amount-item" v-if="selectedCoupon && discountAmount > 0">
          <span>优惠券</span>
          <span class="discount">-¥{{ discountAmount.toFixed(2) }}</span>
        </div>
        <div class="amount-item total">
          <span>实付金额</span>
          <span class="price">¥{{ finalAmount.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 提交订单 -->
      <div class="submit-section">
        <div class="amount-info">
          <span>实付金额：</span>
          <span class="price">¥{{ finalAmount.toFixed(2) }}</span>
        </div>
        <el-button type="danger" size="large" @click="submitOrder" :loading="submitting">
          提交订单
        </el-button>
      </div>
    </div>

    <!-- 地址编辑对话框 -->
    <el-dialog
      v-model="showAddressDialog"
      :title="editingAddress ? '编辑地址' : '新增地址'"
      width="500px"
    >
      <el-form :model="addressForm" label-width="80px" ref="addressFormRef" :rules="addressRules">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号码" prop="receiverPhone">
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号码" />
        </el-form-item>
        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择所在地区"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detailAddress">
          <el-input
            v-model="addressForm.detailAddress"
            type="textarea"
            placeholder="请输入详细地址"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">确定</el-button>
      </template>
    </el-dialog>

    <!-- 优惠券选择对话框 -->
    <el-dialog
      v-model="showCouponDialog"
      title="选择优惠券"
      width="600px"
      destroy-on-close
      class="coupon-dialog"
    >
      <div class="coupon-list">
        <div v-if="availableCoupons.length === 0" class="empty-coupons">
          <el-empty description="暂无可用优惠券"></el-empty>
        </div>
        <transition-group name="coupon-list" tag="div" class="coupon-items">
          <div
            v-for="coupon in availableCoupons"
            :key="coupon.userCouponId"
            class="coupon-item"
            :class="{ 
              active: tempSelectedCoupon?.userCouponId === coupon.userCouponId,
              'selected-coupon': selectedCoupon?.userCouponId === coupon.userCouponId
            }"
            @click="selectCoupon(coupon)"
          >
            <div class="coupon-amount">
              <span class="currency">¥</span>
              <span class="number">{{ coupon.value }}</span>
            </div>
            <div class="coupon-info">
              <div class="coupon-name">{{ coupon.couponName }}</div>
              <div class="coupon-condition" v-if="coupon.threshold">满{{ coupon.threshold }}元可用</div>
              <div class="coupon-time">有效期至：{{ formatDate(coupon.endTime) }}</div>
            </div>
            <div class="coupon-action">
              <el-button
                :type="tempSelectedCoupon?.userCouponId === coupon.userCouponId ? 'danger' : 'default'"
                size="small"
                class="select-btn"
              >
                {{ tempSelectedCoupon?.userCouponId === coupon.userCouponId ? '已选择' : '选择' }}
              </el-button>
            </div>
          </div>
        </transition-group>
      </div>
      <template #footer>
        <el-button @click="cancelCouponSelection">取消</el-button>
        <el-button type="primary" @click="confirmCoupon" :disabled="!tempSelectedCoupon">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowRight, Money, CreditCard, Wallet } from '@element-plus/icons-vue'
import { useCartStore } from '../stores/cart'
import { addressApi } from '../api/address'
import { couponApi } from '../api/coupon'
import { orderApi } from '../api/order'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const loading = ref(false)
const submitting = ref(false)

// 收货地址
const addresses = ref([])
const selectedAddress = ref(null)
const showAddressDialog = ref(false)
const editingAddress = ref(null)
const addressFormRef = ref(null)

const addressForm = ref({
  receiverName: '',
  receiverPhone: '',
  region: [],
  detailAddress: '',
  isDefault: false
})

const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 地区选项（示例数据，实际应从API获取）
const regionOptions = [
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '南山区', label: '南山区' },
          { value: '福田区', label: '福田区' },
          { value: '罗湖区', label: '罗湖区' }
        ]
      },
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '海珠区', label: '海珠区' },
          { value: '越秀区', label: '越秀区' }
        ]
      }
    ]
  }
]

// 从路由参数中获取商品信息
const orderItems = ref([])

// 优惠券
const availableCoupons = ref([])
const selectedCoupon = ref(null)
const showCouponDialog = ref(false)
const tempSelectedCoupon = ref(null)
const discountAmount = ref(0)

// 支付方式
const paymentMethods = [
  { id: '1', name: '支付宝', icon: 'Money' },
  { id: '2', name: '微信支付', icon: 'Wallet' },
  { id: '3', name: '银行卡', icon: 'CreditCard' }
]

const selectedPayment = ref('1')

// 金额计算
const shipping = ref(0)

// 计算商品总价
const totalAmount = computed(() => {
  return orderItems.value.reduce((total, item) => {
    return total + (item.price * item.quantity)
  }, 0)
})

// 计算最终支付金额
const finalAmount = computed(() => {
  return Math.max(0, totalAmount.value + shipping.value - discountAmount.value)
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 初始化数据
onMounted(async () => {
  loading.value = true
  try {
    // 解析路由参数中的商品信息
    const itemsParam = route.query.items
    if (itemsParam) {
      orderItems.value = JSON.parse(itemsParam)
    } else {
      ElMessage.error('订单信息加载失败')
      router.push('/cart')
      return
    }

    // 获取用户地址列表
    await fetchAddresses()
    
    // 获取用户优惠券列表
    await fetchCoupons()
    
    // 设置默认地址
    if (addresses.value.length > 0) {
      const defaultAddress = addresses.value.find(addr => addr.isDefault === 1)
      selectedAddress.value = defaultAddress || addresses.value[0]
    }
  } catch (error) {
    console.error('初始化数据失败:', error)
    ElMessage.error('数据加载失败，请重试')
  } finally {
    loading.value = false
  }
})

// 获取用户地址列表
const fetchAddresses = async () => {
  try {
    const response = await addressApi.getAddresses()
    if (response.code === 200) {
      addresses.value = response.data || []
    } else {
      throw new Error(response.msg || '获取地址列表失败')
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
    ElMessage.error('获取地址列表失败')
  }
}

// 获取用户优惠券列表
const fetchCoupons = async () => {
  try {
    console.log('开始获取用户优惠券列表');
    const response = await couponApi.getUserCoupons('unused');
    console.log('优惠券API响应:', response);

    if (response && response.code === 200) {
      // 确保正确处理API响应
      availableCoupons.value = (response.data || []).map(coupon => {
        console.log('处理优惠券:', coupon);
        return {
          userCouponId: coupon.id,
          couponId: coupon.couponId,
          couponName: coupon.couponName,
          value: coupon.value,
          threshold: coupon.threshold,
          type: coupon.type || 'fixed', // 默认为固定金额
          discountAmount: coupon.value, // 直接使用value作为折扣金额
          endTime: coupon.endTime || new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString() // 如果没有结束时间，默认30天
        };
      });

      console.log('处理后的优惠券数据:', availableCoupons.value);

      // 过滤出可用的优惠券（根据订单金额）
      const amount = totalAmount.value;
      availableCoupons.value = availableCoupons.value.filter(coupon => {
        return !coupon.threshold || amount >= coupon.threshold;
      });

      console.log('过滤后的可用优惠券:', availableCoupons.value);
    } else {
      throw new Error(response?.msg || '获取优惠券列表失败');
    }
  } catch (error) {
    console.error('获取优惠券列表失败:', error);
    ElMessage.error('获取优惠券列表失败');
  }
};

// 计算优惠券折扣金额
const calculateDiscount = (coupon) => {
  if (!coupon) {
    discountAmount.value = 0;
    return;
  }
  
  // 直接使用优惠券的value值作为折扣金额
  discountAmount.value = coupon.value || 0;
  console.log(`应用优惠券折扣: ${discountAmount.value}元`);
};

// 地址相关方法
const selectAddress = (address) => {
  selectedAddress.value = address
}

const editAddress = (address) => {
  editingAddress.value = address
  addressForm.value = {
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    region: [address.province, address.city, address.district],
    detailAddress: address.detailAddress,
    isDefault: address.isDefault === 1
  }
  showAddressDialog.value = true
}

const deleteAddress = async (address) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      type: 'warning'
    })
    
    const response = await addressApi.deleteAddress(address.addressId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      await fetchAddresses()
      
      if (selectedAddress.value?.addressId === address.addressId) {
        selectedAddress.value = addresses.value[0] || null
      }
    } else {
      throw new Error(response.msg || '删除地址失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地址失败:', error)
      ElMessage.error('删除地址失败')
    }
  }
}

const saveAddress = async () => {
  if (!addressFormRef.value) return
  
  try {
    await addressFormRef.value.validate()
    
    const [province, city, district] = addressForm.value.region
    const addressData = {
      receiverName: addressForm.value.receiverName,
      receiverPhone: addressForm.value.receiverPhone,
      province,
      city,
      district,
      detailAddress: addressForm.value.detailAddress,
      isDefault: addressForm.value.isDefault ? 1 : 0
    }
    
    let response
    if (editingAddress.value) {
      response = await addressApi.updateAddress(editingAddress.value.addressId, addressData)
    } else {
      response = await addressApi.addAddress(addressData)
    }
    
    if (response.code === 200) {
      ElMessage.success(editingAddress.value ? '修改成功' : '添加成功')
      showAddressDialog.value = false
      await fetchAddresses()
      
      // 如果是新增地址且没有选择地址，则选择新增的地址
      if (!editingAddress.value && !selectedAddress.value && addresses.value.length > 0) {
        selectedAddress.value = addresses.value[0]
      }
    } else {
      throw new Error(response.msg || (editingAddress.value ? '修改地址失败' : '添加地址失败'))
    }
  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error(error.message || '保存地址失败')
  }
}

// 优惠券相关方法
const selectCoupon = (coupon) => {
  tempSelectedCoupon.value = coupon;
};

const confirmCoupon = () => {
  selectedCoupon.value = tempSelectedCoupon.value;
  showCouponDialog.value = false;
  
  // 计算优惠金额
  calculateDiscount(selectedCoupon.value);
};

const cancelCouponSelection = () => {
  // 重置临时选择的优惠券
  tempSelectedCoupon.value = selectedCoupon.value;
  showCouponDialog.value = false;
};

// 提交订单
const submitOrder = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  if (!selectedPayment.value) {
    ElMessage.warning('请选择支付方式')
    return
  }
  
  submitting.value = true
  
  try {
    // 构建订单数据
    const orderData = {
      addressId: selectedAddress.value.addressId,
      couponId: selectedCoupon.value?.couponId,
      items: orderItems.value.map(item => ({
        productId: item.productId || item.id,
        quantity: item.quantity,
        skuId: item.skuId || null
      })),
      remark: ''
    }
    
    // 创建订单
    const response = await orderApi.createOrder(orderData)
    
    if (response.code === 200) {
      const orderId = response.data
      
      // 支付订单
      const payResponse = await orderApi.payOrder(orderId, selectedPayment.value)
      
      if (payResponse.code === 200) {
        ElMessage.success('订单提交成功')
        
        // ✅ 只清空本地购物车状态，不再发多余的删除请求
        if (cartStore.clearCart) {
          cartStore.clearCart()
        } else if (cartStore.fetchCartItems) {
          await cartStore.fetchCartItems()
        }
        
        // 跳转到订单列表页面
        router.push('/profile/orders')
      } else {
        throw new Error(payResponse.msg || '支付失败')
      }
    } else {
      throw new Error(response.msg || '创建订单失败')
    }
  } catch (error) {
    console.error('提交订单失败:', error)
    ElMessage.error(error.message || '订单提交失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
/* 基础布局 */
.checkout-container {
  max-width: 1400px;
  margin: 20px auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.checkout-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 通用部分样式 */
.section {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 25px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.section:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* 地址部分样式 */
.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.address-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.address-item:hover {
  transform: translateY(-5px);
  border-color: rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.address-item.active {
  border-color: #409EFF;
  background: rgba(64, 158, 255, 0.1);
}

.address-info {
  margin-bottom: 15px;
}

.contact {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.name {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.phone {
  color: rgba(255, 255, 255, 0.8);
}

.address-detail {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  line-height: 1.6;
}

.address-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.add-address {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px dashed rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: rgba(255, 255, 255, 0.8);
}

.add-address:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
  color: #fff;
}

/* 商品信息部分样式 */
.order-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  display: grid;
  grid-template-columns: 80px 1fr auto auto auto;
  gap: 15px;
  align-items: center;
  padding: 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.item-name {
  font-size: 14px;
  color: #fff;
  font-weight: 500;
}

.item-specs {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.item-price,
.item-quantity,
.item-total {
  color: #fff;
  font-weight: 500;
}

/* 优惠券部分样式 */
.coupon-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.coupon-selector:hover {
  background: rgba(255, 255, 255, 0.1);
}

.label {
  color: #fff;
  font-weight: 500;
}

.selected-coupon {
  display: flex;
  align-items: center;
  gap: 10px;
}

.placeholder {
  color: rgba(255, 255, 255, 0.6);
}

/* 支付方式部分样式 */
.payment-methods {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.payment-method {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.payment-method:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-3px);
}

.payment-method.active {
  border-color: #409EFF;
  background: rgba(64, 158, 255, 0.1);
}

/* 金额部分样式 */
.amount-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: rgba(255, 255, 255, 0.8);
}

.amount-item.total {
  margin-top: 10px;
  padding-top: 15px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.discount {
  color: #f56c6c;
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

/* 提交订单部分样式 */
.submit-section {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  margin-top: 20px;
}

.amount-info {
  font-size: 16px;
  color: #fff;
}

/* 优惠券对话框样式 */
:deep(.el-dialog) {
  background: rgba(30, 30, 40, 0.9) !important;
  border-radius: 16px !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
}

:deep(.el-dialog__header) {
  padding: 20px !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-dialog__title) {
  color: #fff !important;
  font-weight: 600 !important;
  font-size: 18px !important;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: #fff !important;
}

:deep(.el-dialog__body) {
  padding: 20px !important;
  color: #fff !important;
}

:deep(.el-dialog__footer) {
  padding: 15px 20px !important;
  border-top: 1px solid rgba(255, 255, 255, 0.1) !important;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-height: 400px;
  overflow-y: auto;
}

.coupon-list::-webkit-scrollbar {
  width: 6px;
}

.coupon-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.coupon-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.coupon-list::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.coupon-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.coupon-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.coupon-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.coupon-item.active {
  border-color: #f56c6c;
  background: rgba(245, 108, 108, 0.1);
}

.coupon-item.active::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 40px 40px 0;
  border-color: transparent #f56c6c transparent transparent;
}

.coupon-item.active::after {
  content: '✓';
  position: absolute;
  top: 5px;
  right: 8px;
  color: #fff;
  font-size: 14px;
  font-weight: bold;
}

.coupon-item.selected-coupon {
  border-color: rgba(245, 108, 108, 0.5);
  background: rgba(245, 108, 108, 0.05);
}

.coupon-amount {
  display: flex;
  align-items: baseline;
  color: #f56c6c;
  font-weight: 600;
  background: rgba(245, 108, 108, 0.1);
  padding: 10px 15px;
  border-radius: 8px;
  min-width: 80px;
  justify-content: center;
}

.currency {
  font-size: 16px;
}

.number {
  font-size: 24px;
}

.coupon-info {
  flex: 1;
}

.coupon-name {
  font-size: 16px;
  color: #fff;
  margin-bottom: 5px;
  font-weight: 500;
}

.coupon-condition {
  font-size: 13px;
  color: #f56c6c;
  margin-bottom: 5px;
}

.coupon-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.coupon-action {
  display: flex;
  align-items: center;
}

.select-btn {
  min-width: 80px;
}

.empty-coupons {
  padding: 30px;
  text-align: center;
}

/* 优惠券列表动画 */
.coupon-list-enter-active,
.coupon-list-leave-active {
  transition: all 0.3s ease;
}

.coupon-list-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.coupon-list-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

.coupon-list-move {
  transition: transform 0.5s ease;
}

:deep(.el-empty__description p) {
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-button) {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: #fff !important;
}

:deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.2) !important;
  border-color: rgba(255, 255, 255, 0.3) !important;
}

:deep(.el-button--primary) {
  background: rgba(64, 158, 255, 0.2) !important;
  border-color: rgba(64, 158, 255, 0.3) !important;
}

:deep(.el-button--primary:hover) {
  background: rgba(64, 158, 255, 0.3) !important;
  border-color: rgba(64, 158, 255, 0.4) !important;
}

:deep(.el-button--danger) {
  background: rgba(245, 108, 108, 0.2) !important;
  border-color: rgba(245, 108, 108, 0.3) !important;
}

:deep(.el-button--danger:hover) {
  background: rgba(245, 108, 108, 0.3) !important;
  border-color: rgba(245, 108, 108, 0.4) !important;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .checkout-container {
    padding: 15px;
  }
  
  .section {
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .order-item {
    grid-template-columns: 60px 1fr;
    gap: 10px;
  }
  
  .item-price,
  .item-quantity,
  .item-total {
    grid-column: 2;
  }
  
  .payment-methods {
    grid-template-columns: 1fr;
  }
  
  .submit-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .amount-info {
    text-align: center;
  }
}

@media (max-width: 576px) {
  .checkout-container {
    padding: 10px;
  }
  
  .section {
    padding: 15px;
  }
  
  .section-title {
    font-size: 16px;
  }
  
  .address-list {
    grid-template-columns: 1fr;
  }
  
  .order-item {
    grid-template-columns: 50px 1fr;
  }
  
  .item-image {
    width: 50px;
    height: 50px;
  }
}
</style> 