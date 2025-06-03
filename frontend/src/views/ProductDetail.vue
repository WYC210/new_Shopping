<template>
  <div class="product-detail-container" v-loading="loading">
    <el-empty v-if="!loading && !product" description="商品不存在"></el-empty>
    
    <template v-else>
      <div class="product-content">
        <!-- Product Images -->
        <div class="product-images">
          <div class="image-container">
            <div class="main-image" 
              ref="mainImageRef"
              @mousemove="handleMouseMove"
              @mouseenter="handleMouseEnter"
              @mouseleave="handleMouseLeave"
            >
              <el-image
                :src="currentImage"
                fit="contain"
                :preview-src-list="product?.images"
                :initial-index="currentImageIndex"
              />
              <div class="magnifier-lens" v-show="showMagnifier" :style="lensStyle"></div>
            </div>
            <div class="magnifier-preview" v-show="showMagnifier">
              <div class="magnifier-image" :style="magnifierStyle"></div>
            </div>
          </div>
          <div class="thumbnail-list">
            <div
              v-for="(image, index) in product?.images"
              :key="index"
              class="thumbnail"
              :class="{ active: currentImageIndex === index }"
              @click="handleThumbnailClick(index)"
              @mouseenter="handleThumbnailHover(index)"
            >
              <el-image :src="image" fit="cover" />
            </div>
          </div>
        </div>

        <!-- Product Info -->
        <div class="product-info">
          <h1 class="product-name">{{ product?.name }}</h1>
          
          <div class="product-meta">
            <div class="sales-info">
              <span class="label">销量</span>
              <span class="value">{{ product?.sales }}件</span>
            </div>
            <div class="rating-info">
              <span class="label">评分</span>
              <el-rate
                :model-value="product?.rating"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>

          <div class="price-section">
            <div class="current-price">
              <span class="label">价格</span>
              <span class="price">¥{{ selectedSku ? selectedSku.price : product?.price }}</span>
              <span class="original-price" v-if="selectedSku ? selectedSku.originalPrice : product?.originalPrice">
                ¥{{ selectedSku ? selectedSku.originalPrice : product?.originalPrice }}
              </span>
            </div>
            <div class="discount" v-if="selectedSku ? selectedSku.discount : product?.discount">
              <el-tag type="danger" effect="dark">{{ selectedSku ? selectedSku.discount : product?.discount }}折</el-tag>
            </div>
          </div>

          <!-- SKU Selection -->
          <div class="sku-section" v-if="product?.skus && product.skus.length">
            <div v-for="(specValues, specName) in getSpecifications()" :key="specName" class="spec-group">
              <span class="label">{{ specName }}</span>
              <div class="spec-options">
                <el-radio-group v-model="selectedSpecs[specName]" @change="handleSpecChange">
                  <el-radio-button 
                    v-for="option in specValues" 
                    :key="option"
                    :label="option"
                    :disabled="!isSpecAvailable(specName, option)"
                  >
                    {{ option }}
                  </el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </div>

          <div class="product-tags" v-if="product?.tags && product.tags.length">
            <span class="label">标签</span>
            <el-tag
              v-for="tag in product.tags"
              :key="tag"
              class="tag"
              effect="plain"
            >
              {{ tag }}
            </el-tag>
          </div>

          <div class="quantity-section">
            <span class="label">数量</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="selectedSku ? selectedSku.stock : product?.stock"
              size="large"
            />
            <span class="stock">库存: {{ selectedSku ? selectedSku.stock : product?.stock }}件</span>
          </div>

          <div class="action-buttons">
            <div class="main-actions">
              <el-button
                type="danger"
                size="large"
                :icon="ShoppingCart"
                @click="addToCart"
                :disabled="product?.skus && product.skus.length > 0 && !selectedSku"
              >
                加入购物车
              </el-button>
              <el-button
                type="danger"
                size="large"
                :icon="ShoppingBag"
                @click="buyNow"
                :disabled="product?.skus && product.skus.length > 0 && !selectedSku"
              >
                立即购买
              </el-button>
            </div>
            <el-button
              class="favorite-btn"
              :icon="isFavorite ? StarFilled : Star"
              :type="isFavorite ? 'danger' : 'default'"
              circle
              @click="toggleFavorite"
            />
          </div>

          <div class="service-info">
            <div class="service-item">
              <el-icon><Check /></el-icon>
              <span>正品保证</span>
            </div>
            <div class="service-item">
              <el-icon><Van /></el-icon>
              <span>极速发货</span>
            </div>
            <div class="service-item">
              <el-icon><Service /></el-icon>
              <span>无忧退换</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Details Tabs -->
      <div class="product-details">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="details">
            <div class="detail-content" v-html="product?.description"></div>
          </el-tab-pane>
          <el-tab-pane label="规格参数" name="specs">
            <el-descriptions :column="2" border>
              <el-descriptions-item
                v-for="(spec, key) in product?.specifications"
                :key="key"
                :label="key"
              >
                {{ spec }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="用户评价" name="reviews">
            <div class="reviews-section">
              <div class="review-summary">
                <div class="rating-overview">
                  <div class="average-rating">
                    <span class="rating-number">{{ product?.rating }}</span>
                    <el-rate :model-value="product?.rating" disabled />
                    <span class="rating-count">共{{ product?.reviewCount }}条评价</span>
                  </div>
                </div>
              </div>
              <div class="review-list">
                <div v-for="review in product?.reviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <el-avatar :src="review.userAvatar" />
                    <div class="review-info">
                      <span class="username">{{ review.username }}</span>
                      <el-rate :model-value="review.rating" disabled />
                    </div>
                    <span class="review-date">{{ review.date }}</span>
                  </div>
                  <div class="review-content">{{ review.content }}</div>
                  <div class="review-images" v-if="review.images && review.images.length">
                    <el-image
                      v-for="(image, index) in review.images"
                      :key="index"
                      :src="image"
                      :preview-src-list="review.images"
                      fit="cover"
                    />
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import { useProductStore } from '../stores/product'
import { ElMessage } from 'element-plus'
import { ShoppingCart, ShoppingBag, Star, StarFilled, Check, Van, Service } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const productStore = useProductStore()

const quantity = ref(1)
const activeTab = ref('details')
const currentImageIndex = ref(0)
const showMagnifier = ref(false)
const selectedSpecs = ref({})
const selectedSku = ref(null)
const isFavorite = ref(false)
const mainImageRef = ref(null)

const lensPosition = ref({ x: 0, y: 0 })
const magnifierPosition = ref({ x: 0, y: 0 })
const LENS_SIZE = 100
const MAGNIFIER_WIDTH = 400
const MAGNIFIER_HEIGHT = 400
let isMouseInImage = false

const throttle = (fn, delay) => {
  let lastCall = 0
  return function (...args) {
    const now = performance.now()
    if (now - lastCall >= delay) {
      lastCall = now
      return fn.apply(this, args)
    }
  }
}

const product = computed(() => {
  if (!productStore.productDetails) return null
  const details = JSON.parse(JSON.stringify(productStore.productDetails))

  if (details.imageUrls && !details.images) {
    details.images = details.imageUrls
  }

  if (details.specifications && typeof details.specifications === 'string') {
    try {
      details.specifications = JSON.parse(details.specifications)
    } catch (error) {
      console.error('Failed to parse specifications:', error)
    }
  }

  if (details.skus && details.skus.length > 0) {
    details.skus = details.skus.map(sku => ({
      ...sku,
      specs: sku.attributes || {}
    }))
  }

  return details
})

const loading = computed(() => productStore.loading.detail)

const currentImage = computed(() => {
  if (!product.value?.images) return ''
  return product.value.images[currentImageIndex.value]
})

const lensStyle = computed(() => ({
  left: `${lensPosition.value.x}px`,
  top: `${lensPosition.value.y}px`
}))

const magnifierStyle = computed(() => ({
  backgroundImage: `url(${currentImage.value})`,
  backgroundSize: `${MAGNIFIER_WIDTH * 2}px ${MAGNIFIER_HEIGHT * 2}px`,
  backgroundPosition: `${magnifierPosition.value.x}px ${magnifierPosition.value.y}px`
}))

const handleMouseEnter = () => {
  if (!currentImage.value) return
  isMouseInImage = true
  const img = new Image()
  img.src = currentImage.value
  img.onload = () => {
    if (isMouseInImage) {
      showMagnifier.value = true
    }
  }
}

const handleMouseLeave = () => {
  isMouseInImage = false
  showMagnifier.value = false
}

const handleMouseMove = throttle((event) => {
  if (!isMouseInImage || !currentImage.value) {
    showMagnifier.value = false
    return
  }

  try {
    const imgElement = event.currentTarget.querySelector('.el-image img')
    if (!imgElement) return

    const { left, top, width, height } = imgElement.getBoundingClientRect()
    const x = event.clientX - left
    const y = event.clientY - top

    if (x < 0 || x > width || y < 0 || y > height) {
      showMagnifier.value = false
      return
    }

    showMagnifier.value = true

    const lensX = Math.min(Math.max(0, x - LENS_SIZE / 2), width - LENS_SIZE)
    const lensY = Math.min(Math.max(0, y - LENS_SIZE / 2), height - LENS_SIZE)

    lensPosition.value = {
      x: lensX,
      y: lensY
    }

    // 使用2倍的放大比例
    const scale = 2
    // 计算背景位置，使鼠标位置在放大镜中居中显示
    const bgX = -((x / width) * (width * scale) - MAGNIFIER_WIDTH / 2)
    const bgY = -((y / height) * (height * scale) - MAGNIFIER_HEIGHT / 2)

    // 限制背景位置，防止显示空白区域
    const maxBgX = -(width * scale - MAGNIFIER_WIDTH)
    const maxBgY = -(height * scale - MAGNIFIER_HEIGHT)

    magnifierPosition.value = {
      x: Math.min(0, Math.max(bgX, maxBgX)),
      y: Math.min(0, Math.max(bgY, maxBgY))
    }
  } catch (error) {
    console.error('Error in handleMouseMove:', error)
    showMagnifier.value = false
  }
}, 16)

const handleThumbnailClick = (index) => {
  currentImageIndex.value = index
}

const handleThumbnailHover = (index) => {
  currentImageIndex.value = index
}

const getSpecifications = () => {
  if (!product.value?.skus || !product.value.skus.length) return {}

  const specs = {}

  product.value.skus.forEach(sku => {
    const skuSpecs = sku.attributes || sku.specs || {}

    Object.entries(skuSpecs).forEach(([key, value]) => {
      if (!specs[key]) {
        specs[key] = []
      }
      if (!specs[key].includes(value)) {
        specs[key].push(value)
      }
    })
  })

  return specs
}

const isSpecAvailable = (specName, option) => {
  if (!product.value?.skus) return true

  const tempSpecs = { ...selectedSpecs.value, [specName]: option }

  return product.value.skus.some(sku => {
    const skuSpecs = sku.attributes || sku.specs || {}

    return Object.entries(tempSpecs).every(([key, value]) => {
      return !value || skuSpecs[key] === value
    })
  })
}

const handleSpecChange = () => {
  if (!product.value?.skus) return

  selectedSku.value = product.value.skus.find(sku => {
    const skuSpecs = sku.attributes || sku.specs || {}

    return Object.entries(selectedSpecs.value).every(([key, value]) => {
      return !value || skuSpecs[key] === value
    })
  })
}

const addToCart = () => {
  if (!product.value) return

  if (product.value.skus && !selectedSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }

  const productId = product.value.productId || product.value.id
  const skuId = selectedSku.value ? selectedSku.value.skuId : null

  cartStore.addItem(productId, skuId, quantity.value)
  ElMessage.success('已添加到购物车')
}

const buyNow = () => {
  if (!product.value) return

  if (product.value.skus && !selectedSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }

  const productId = product.value.productId || product.value.id
  const productImage = product.value.images?.[0] || ''
  const skuId = selectedSku.value ? selectedSku.value.skuId : null

  const checkoutItem = {
    id: productId,
    productId: productId,
    skuId: skuId,
    name: product.value.name,
    price: selectedSku.value ? selectedSku.value.price : product.value.price,
    image: productImage,
    quantity: quantity.value,
    specs: selectedSku.value ? selectedSku.value.attributes : null
  }

  router.push({
    name: 'checkout',
    query: {
      items: JSON.stringify([checkoutItem])
    }
  })
}

const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  ElMessage.success(isFavorite.value ? '已添加到收藏' : '已取消收藏')
}

const handleGlobalMouseMove = throttle((event) => {
  if (!showMagnifier.value || !mainImageRef.value || !isMouseInImage) return

  const imageRect = mainImageRef.value.getBoundingClientRect()
  const { clientX, clientY } = event

  if (
    clientX < imageRect.left ||
    clientX > imageRect.right ||
    clientY < imageRect.top ||
    clientY > imageRect.bottom
  ) {
    isMouseInImage = false
    showMagnifier.value = false
  }
}, 16)

onMounted(() => {
  const productId = route.params.id
  if (productId) {
    productStore.fetchProductDetail(productId)
  }

  window.addEventListener('mousemove', handleGlobalMouseMove)
})

onBeforeUnmount(() => {
  productStore.clearCurrentProduct()
  window.removeEventListener('mousemove', handleGlobalMouseMove)
})

watch(currentImage, () => {
  showMagnifier.value = false
})
</script>

<style scoped>
.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.product-content {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
}

/* Product Images */
.product-images {
  flex: 0 0 500px;
}

.image-container {
  position: relative;
  display: flex;
  gap: 20px;
}

.main-image {
  position: relative;
  width: 500px;
  height: 500px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  cursor: move;
}

.main-image .el-image {
  width: 100%;
  height: 100%;
}

.magnifier-lens {
  position: absolute;
  width: 100px;
  height: 100px;
  border: 2px solid #409EFF;
  background-color: rgba(255, 255, 255, 0.1);
  pointer-events: none;
  z-index: 10;
}

.magnifier-preview {
  position: absolute;
  left: 520px;
  top: 0;
  width: 400px;
  height: 400px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.magnifier-image {
  width: 100%;
  height: 100%;
  background-repeat: no-repeat;
  background-color: white;
}

.thumbnail-list {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
}

.thumbnail:hover,
.thumbnail.active {
  border-color: #409EFF;
}

.thumbnail .el-image {
  width: 100%;
  height: 100%;
}

/* Product Info */
.product-info {
  flex: 1;
}

.product-name {
  font-size: 24px;
  color: #303133;
  margin: 0 0 20px;
}

.product-meta {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.sales-info,
.rating-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label {
  color: #909399;
  font-size: 14px;
}

.value {
  color: #303133;
  font-size: 14px;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.current-price {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
}

.original-price {
  font-size: 16px;
  color: #909399;
  text-decoration: line-through;
}

.product-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.tag {
  margin-right: 10px;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 30px;
}

.stock {
  margin-left: 20px;
  color: #909399;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.main-actions {
  display: flex;
  gap: 20px;
  flex: 1;
}

.main-actions .el-button {
  flex: 1;
  height: 50px;
  font-size: 16px;
}

.favorite-btn {
  width: 50px;
  height: 50px;
  padding: 0;
  font-size: 20px;
}

.service-info {
  display: flex;
  justify-content: space-around;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
}

/* Product Details */
.product-details {
  margin-top: 40px;
}

.detail-content {
  padding: 20px;
}

.reviews-section {
  padding: 20px;
}

.review-summary {
  margin-bottom: 30px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.rating-overview {
  display: flex;
  align-items: center;
  gap: 40px;
}

.average-rating {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rating-number {
  font-size: 36px;
  color: #f56c6c;
  font-weight: bold;
}

.rating-count {
  color: #909399;
  font-size: 14px;
}

.review-item {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.review-info {
  flex: 1;
}

.username {
  font-weight: bold;
  color: #303133;
}

.review-date {
  color: #909399;
  font-size: 14px;
}

.review-content {
  margin: 10px 0;
  color: #606266;
  line-height: 1.6;
}

.review-images {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.review-images .el-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
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

/* SKU Selection Styles */
.sku-section {
  margin: 20px 0;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.spec-group {
  margin-bottom: 15px;
}

.spec-group:last-child {
  margin-bottom: 0;
}

.spec-options {
  margin-top: 10px;
}

.spec-options :deep(.el-radio-button__inner) {
  min-width: 80px;
  text-align: center;
}

.spec-options :deep(.el-radio-button.is-disabled .el-radio-button__inner) {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
  color: #c0c4cc;
  cursor: not-allowed;
}

/* Responsive Styles */
@media (max-width: 768px) {
  .product-content {
    flex-direction: column;
  }
  
  .product-images {
    flex: 0 0 auto;
    width: 100%;
  }
  
  .main-image {
    width: 100%;
    height: auto;
    aspect-ratio: 1;
  }
  
  .thumbnail-list {
    overflow-x: auto;
    padding-bottom: 10px;
  }
  
  .thumbnail {
    flex: 0 0 60px;
    height: 60px;
  }
  
  .spec-options :deep(.el-radio-button__inner) {
    min-width: 60px;
    padding: 8px 15px;
  }
}
</style>
