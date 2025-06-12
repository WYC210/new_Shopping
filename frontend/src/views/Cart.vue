<template>
  <div class="cart-container content-container">
    <div class="cart-header">
      <h2>我的购物车</h2>
      <div class="cart-stats">
        <span>共 {{ totalItems }} 件商品</span>
        <span class="divider">|</span>
        <span>已选择 {{ selectedItems.length }} 件</span>
      </div>
    </div>

    <div v-if="cartItems.length === 0" class="empty-cart">
      <el-empty description="购物车还是空的">
        <el-button type="primary" @click="$router.push('/')">
          去购物
        </el-button>
      </el-empty>
    </div>

    <template v-else>
      <div class="cart-content">
        <!-- Cart Items -->
        <div class="cart-items">
          <div class="cart-item" v-for="item in cartItems" :key="item.itemId">
            <div class="item-select">
              <el-checkbox
                v-model="item.isSelected"
                @change="handleToggleSelection(item.itemId, item.isSelected)"
              />
            </div>
            
            <div class="item-image">
              <el-image :src="item.image" fit="cover" />
            </div>
            <div class="item-info">
              <h3 class="item-name" @click="navigateToProduct(item.productId)">
                {{ item.name }}
              </h3>
              <div class="item-meta">
                <span class="item-price">¥{{ item.price }}</span>
                <div class="item-quantity">
                  <el-input-number
                    v-model="item.quantity"
                    :min="1"
                    :max="99"
                    size="small"
                    @change="handleQuantityChange(item.itemId, item.quantity)"
                  />
                </div>
              </div>
            </div>
            <div class="item-actions">
              <el-button
                type="danger"
                :icon="Delete"
                circle
                @click="handleRemoveItem(item.itemId)"
              />
            </div>
          </div>
        </div>

        <!-- Cart Summary -->
        <div class="cart-summary">
          <div class="summary-header">
            <h3>订单摘要</h3>
          </div>
          <div class="summary-content">
            <div class="summary-item">
              <span>商品总价</span>
              <span>¥{{ totalPrice }}</span>
            </div>
            <div class="summary-item">
              <span>运费</span>
              <span>¥{{ shipping }}</span>
            </div>
            <div class="summary-item discount" v-if="discount > 0">
              <span>优惠</span>
              <span>-¥{{ discount }}</span>
            </div>
            <div class="summary-item total">
              <span>应付总额</span>
              <span class="total-price">¥{{ finalPrice }}</span>
            </div>
          </div>
          <div class="summary-actions">
            <el-button
              type="primary"
              size="large"
              :disabled="selectedItems.length === 0"
              @click="handleCheckout"
            >
              结算 ({{ selectedItems.length }})
            </el-button>
          </div>
        </div>
      </div>

      <!-- Recommended Products -->
      <div class="recommended-products">
        <h3>猜你喜欢</h3>
        <el-row :gutter="20">
          <el-col :span="6" v-for="product in recommendedProducts" :key="product.id">
            <div class="product-card" @click="navigateToProduct(product.id)">
              <div class="product-image">
                <el-image :src="product.image" fit="cover" />
                <div class="product-tags" v-if="product.tags && product.tags.length">
                  <el-tag v-for="tag in product.tags" :key="tag" size="small" effect="dark">
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
              <div class="product-info">
                <h4>{{ product.name }}</h4>
                <div class="product-price">
                  <span class="current-price">¥{{ product.price }}</span>
                  <span class="original-price" v-if="product.originalPrice">
                    ¥{{ product.originalPrice }}
                  </span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const loading = ref(false)

const cartItems = computed(() => cartStore.items)
const selectedItems = computed(() => cartStore.selectedItems)
const totalItems = computed(() => cartStore.totalItems)
const selectedCount = computed(() => cartStore.selectedCount)
const totalPrice = computed(() => cartStore.selectedAmount)
console.log("哈哈哈哈")
console.log(cartItems)

const shipping = ref(10)
const discount = ref(50)
const recommendedProducts = ref([
  {
    id: 3,
    name: '推荐商品1',
    price: 399.00,
    originalPrice: 499.00,
    image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&h=200',
    tags: ['热销']
  },
  {
    id: 4,
    name: '推荐商品2',
    price: 299.00,
    originalPrice: 399.00,
    image: 'https://images.unsplash.com/photo-1579586337278-3befd40fd17a?ixlib=rb-4.0.3&auto=format&fit=crop&w=200&h=200',
    tags: ['新品']
  }
])

const finalPrice = computed(() => {
  return totalPrice.value + shipping.value - discount.value
})

const handleQuantityChange = async (itemId, quantity) => {
  try {
    await cartStore.updateItemQuantity(itemId, quantity)
  } catch (error) {
    console.error('Failed to update quantity:', error)
  }
}

const handleRemoveItem = async (itemId) => {
  try {
    if (!itemId) {
      ElMessage.error('商品ID无效，无法删除');
      return;
    }
    
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    console.log('删除购物车商品，ID:', itemId);
    await cartStore.removeItem(itemId)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to remove item:', error)
    }
  }
}

const handleToggleSelection = async (itemId, isSelected) => {
  try {
    await cartStore.toggleItemSelection(itemId, isSelected)
  } catch (error) {
    console.error('Failed to toggle selection:', error)
  }
}

const handleToggleAllSelection = async (isSelected) => {
  try {
    await cartStore.toggleAllItemsSelection(isSelected)
  } catch (error) {
    console.error('Failed to toggle all selection:', error)
  }
}

const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.clearCart()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to clear cart:', error)
    }
  }
}

const handleCheckout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  // 确保所有选中商品都有完整信息，特别是skuId
  const checkoutItems = selectedItems.value.map(item => {
    return {
      ...item,
      productId: item.productId,
      id: item.itemId,
      skuId: item.skuId
    }
  })
  
  router.push({
    name: 'checkout',
    query: {
      items: JSON.stringify(checkoutItems)
    }
  })
}

const navigateToProduct = (productId) => {
  router.push({ name: 'product', params: { id: productId } });
}

onMounted(async () => {
  try {
    // 获取购物车数据
    await cartStore.fetchCartItems()
  } catch (error) {
    console.error('Failed to fetch cart items:', error)
  }
})
</script>

<style scoped>
.cart-container {
  max-width: 1400px;
  margin: 20px auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding: 15px 25px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.cart-header:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.cart-title {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
}

.cart-title .el-icon {
  font-size: 28px;
  color: rgba(255, 255, 255, 0.9);
}

.cart-actions {
  display: flex;
  gap: 15px;
}

.cart-actions .el-button {
  padding: 10px 20px;
  font-size: 15px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.cart-actions .el-button--danger {
  background: rgba(245, 108, 108, 0.9);
  border: none;
}

.cart-actions .el-button--danger:hover {
  background: rgba(245, 108, 108, 1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

.cart-content {
  display: flex;
  gap: 30px;
}

.cart-items {
  flex: 1;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 25px;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.cart-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.item-checkbox {
  margin-right: 20px;
}

.item-image {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  overflow: hidden;
  margin-right: 25px;
  background: rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
}

.item-image:hover {
  transform: scale(1.05);
}

.item-image .el-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  margin-right: 25px;
}

.item-name {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 10px;
  line-height: 1.4;
}

.item-specs {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.item-spec {
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
}

.item-price {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 5px;
}

.item-original-price {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  text-decoration: line-through;
}

.item-quantity {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-right: 25px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 5px;
}

.quantity-selector :deep(.el-input-number__decrease),
.quantity-selector :deep(.el-input-number__increase) {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: #fff;
}

.quantity-selector :deep(.el-input__inner) {
  background: transparent;
  border: none;
  color: #fff;
  text-align: center;
}

.item-total {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin-right: 25px;
  min-width: 100px;
  text-align: right;
}

.item-actions {
  display: flex;
  gap: 10px;
}

.item-actions .el-button {
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.item-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.cart-summary {
  width: 350px;
  flex-shrink: 0;
}

.summary-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 25px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.summary-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.summary-title {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  color: rgba(255, 255, 255, 0.8);
}

.summary-item.total {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.summary-actions {
  margin-top: 25px;
}

.summary-actions .el-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.summary-actions .el-button--primary {
  background: rgba(64, 158, 255, 0.9);
  border: none;
}

.summary-actions .el-button--primary:hover {
  background: rgba(64, 158, 255, 1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.empty-cart {
  text-align: center;
  padding: 50px 0;
  color: rgba(255, 255, 255, 0.8);
}

.empty-cart .el-icon {
  font-size: 64px;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 20px;
}

.empty-cart-text {
  font-size: 18px;
  margin-bottom: 25px;
}

.empty-cart .el-button {
  padding: 12px 30px;
  font-size: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.empty-cart .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

@media (max-width: 1200px) {
  .cart-content {
    flex-direction: column;
  }
  
  .cart-summary {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .cart-container {
    padding: 15px;
  }
  
  .cart-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .cart-item {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }
  
  .item-checkbox {
    margin: 0 0 15px 0;
  }
  
  .item-image {
    margin: 0 0 20px 0;
  }
  
  .item-info {
    margin: 0 0 20px 0;
  }
  
  .item-quantity {
    margin: 0 0 20px 0;
    justify-content: center;
  }
  
  .item-total {
    margin: 0 0 20px 0;
    text-align: center;
  }
  
  .item-actions {
    justify-content: center;
  }
}

@media (max-width: 576px) {
  .cart-container {
    padding: 10px;
  }
  
  .cart-title {
    font-size: 20px;
  }
  
  .cart-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .cart-actions .el-button {
    width: 100%;
  }
  
  .item-name {
    font-size: 16px;
  }
  
  .item-price,
  .item-total {
    font-size: 18px;
  }
  
  .summary-title {
    font-size: 18px;
  }
  
  .summary-item {
    font-size: 14px;
  }
  
  .summary-item.total {
    font-size: 16px;
  }
}
</style> 