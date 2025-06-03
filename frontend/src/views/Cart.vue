<template>
  <div class="cart-container">
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)

const cartItems = computed(() => cartStore.items)
const selectedItems = computed(() => cartStore.selectedItems)
const totalItems = computed(() => cartStore.totalItems)
const selectedCount = computed(() => cartStore.selectedCount)
const totalPrice = computed(() => cartStore.selectedAmount)

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
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ebeef5;
}

.cart-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.cart-stats {
  color: #909399;
  font-size: 14px;
}

.divider {
  margin: 0 10px;
}

.empty-cart {
  padding: 60px 0;
  text-align: center;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cart-content {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
}

.cart-items {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-select {
  margin-right: 20px;
}

.item-image {
  width: 100px;
  height: 100px;
  margin-right: 20px;
  border-radius: 4px;
  overflow: hidden;
}

.item-image .el-image {
  width: 100%;
  height: 100%;
}

.item-info {
  flex: 1;
}

.item-name {
  margin: 0 0 10px;
  font-size: 16px;
  color: #303133;
  cursor: pointer;
}

.item-name:hover {
  color: #409EFF;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.item-quantity {
  width: 120px;
}

.cart-summary {
  width: 300px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.summary-header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.summary-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.summary-content {
  padding: 20px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  color: #606266;
}

.summary-item.discount {
  color: #f56c6c;
}

.summary-item.total {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.total-price {
  color: #f56c6c;
  font-size: 20px;
}

.summary-actions {
  padding: 20px;
  border-top: 1px solid #ebeef5;
}

.summary-actions .el-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.recommended-products {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.recommended-products h3 {
  margin: 0 0 20px;
  font-size: 18px;
  color: #303133;
}

.product-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  position: relative;
  height: 200px;
}

.product-image .el-image {
  width: 100%;
  height: 100%;
}

.product-tags {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  gap: 8px;
}

.product-info {
  padding: 15px;
}

.product-info h4 {
  margin: 0 0 10px;
  font-size: 14px;
  color: #303133;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.current-price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: bold;
}

.original-price {
  color: #909399;
  font-size: 12px;
  text-decoration: line-through;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409EFF 0%, #36D1DC 100%);
  border: none;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #36D1DC 0%, #409EFF 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #f56c6c 0%, #ff9f9f 100%);
  border: none;
}

:deep(.el-button--danger:hover) {
  background: linear-gradient(135deg, #ff9f9f 0%, #f56c6c 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.2);
}
</style> 