<template>
  <div class="product-detail-container content-container" v-loading="loading">
    <el-button class="back-btn" type="primary" @click="goBack" plain :icon="ArrowLeft" size="large">返回</el-button>
    <el-empty v-if="!loading && !product" description="商品不存在"></el-empty>
    
    <template v-else>
      <div class="product-content">
        <!-- Product Images -->
        <div class="product-images">
          <div class="image-container">
            <div class="main-image-container">
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
                class="thumbnail-item"
                :class="{ active: currentImageIndex === index }"
                @click="handleThumbnailClick(index)"
                @mouseenter="handleThumbnailHover(index)"
              >
                <el-image :src="image" fit="cover" />
              </div>
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
              <template v-if="product?.skus && product.skus.length">
                <template v-if="selectedSku">
                  <span class="price">¥{{ selectedSku.price }}</span>
                  <span class="original-price" v-if="selectedSku.originalPrice">
                    ¥{{ selectedSku.originalPrice }}
                  </span>
                </template>
                <template v-else>
                  <span class="price price-placeholder">请选择规格</span>
                </template>
              </template>
              <template v-else>
                <span class="price">¥{{ product?.price }}</span>
                <span class="original-price" v-if="product?.originalPrice">
                  ¥{{ product?.originalPrice }}
                </span>
              </template>
            </div>
            <div class="discount" v-if="selectedSku && selectedSku.discount">
              <el-tag type="danger" effect="dark">{{ selectedSku.discount }}折</el-tag>
            </div>
            <div class="discount" v-else-if="!product?.skus || !product.skus.length">
              <el-tag v-if="product?.discount" type="danger" effect="dark">{{ product.discount }}折</el-tag>
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
            <div class="quantity-wrapper">
              <span class="label">数量</span>
              <div class="quantity-selector">
                <el-input-number
                  v-model="quantity"
                  :min="1"
                  :max="selectedSku ? selectedSku.stock : product?.stock"
                  size="large"
                  class="custom-input-number"
                />
              </div>
            </div>
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
                class="action-btn"
              >
                加入购物车
              </el-button>
              <el-button
                type="danger"
                size="large"
                :icon="ShoppingBag"
                @click="buyNow"
                :disabled="product?.skus && product.skus.length > 0 && !selectedSku"
                class="action-btn"
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
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { ShoppingCart, ShoppingBag, Star, StarFilled, Check, Van, Service, ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const productStore = useProductStore()
const userStore = useUserStore()

const quantity = ref(1)
const activeTab = ref('details')
const currentImageIndex = ref(0)
const showMagnifier = ref(false)
const selectedSpecs = ref({})
const selectedSku = ref(null)
const isFavorite = ref(false)
const mainImageRef = ref(null) // Reference to the main image element

const lensPosition = ref({ x: 0, y: 0 })
const magnifierPosition = ref({ x: 0, y: 0 })
const LENS_SIZE = 100 // Size of the magnifier lens
const MAGNIFIER_WIDTH = 400 // Width of the magnifier preview box
const MAGNIFIER_HEIGHT = 400 // Height of the magnifier preview box
const MAGNIFICATION_SCALE = 3; // How much to magnify the image

let isMouseInImage = false // Flag to track if mouse is currently over the image

// Throttling function to limit the rate of function calls
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

// 生成随机销量（100-9999之间）
const randomSales = computed(() => {
  return Math.floor(Math.random() * (9999 - 100 + 1)) + 100
})

// 生成随机评分（3.5-5.0之间，保留一位小数）
const randomRating = computed(() => {
  return (Math.random() * (5.0 - 3.5) + 3.5).toFixed(1)
})

// 生成随机评价数量（10-999之间）
const randomReviewCount = computed(() => {
  return Math.floor(Math.random() * (999 - 10 + 1)) + 10
})

// Computed property for product details, normalizing data if necessary
const product = computed(() => {
  if (!productStore.productDetails) return null
  const details = JSON.parse(JSON.stringify(productStore.productDetails))

  // Ensure 'images' array exists, using 'imageUrls' if present
  if (details.imageUrls && !details.images) {
    details.images = details.imageUrls
  }

  // Parse specifications if they are a string
  if (details.specifications && typeof details.specifications === 'string') {
    try {
      details.specifications = JSON.parse(details.specifications)
    } catch (error) {
      console.error('Failed to parse specifications:', error)
    }
  }

  // 兼容skuCode自动补充attributes/specs
  if (details.skus && details.skus.length > 0) {
    details.skus = details.skus.map(sku => {
      // 如果attributes/specs为空，尝试解析skuCode
      if ((!(sku.attributes && Object.keys(sku.attributes).length) || Object.keys(sku.attributes).length === 0) && sku.skuCode) {
        const parts = sku.skuCode.split('-')
        // 这里假设skuCode格式: 型号-容量-颜色
        sku.attributes = {
          '容量': parts[1] ? (parts[1].includes('G') ? parts[1] : parts[1] + 'G') : '',
          '颜色': parts[2] === 'BLK' ? '黑色' : parts[2] === 'WHT' ? '白色' : parts[2] || ''
        }
      }
      return {
        ...sku,
        specs: sku.attributes || {}
      }
    })
  }

  // 添加随机生成的销量和评分
  details.sales = randomSales.value
  details.rating = parseFloat(randomRating.value)
  details.reviewCount = randomReviewCount.value

  return details
})

// Computed property for product loading state
const loading = computed(() => productStore.loading.detail)

// Computed property for the currently displayed main image
const currentImage = computed(() => {
  if (!product.value?.images || product.value.images.length === 0) return ''
  return product.value.images[currentImageIndex.value]
})

// Computed style for the magnifier lens
const lensStyle = computed(() => ({
  left: `${lensPosition.value.x}px`,
  top: `${lensPosition.value.y}px`,
  width: `${LENS_SIZE}px`,
  height: `${LENS_SIZE}px`,
}))

// Computed style for the magnifier preview, handling background position and size
const magnifierStyle = computed(() => ({
  backgroundImage: `url(${currentImage.value})`,
  backgroundSize: `${mainImageRef.value ? mainImageRef.value.offsetWidth * MAGNIFICATION_SCALE : 0}px ${mainImageRef.value ? mainImageRef.value.offsetHeight * MAGNIFICATION_SCALE : 0}px`,
  backgroundPosition: `${magnifierPosition.value.x}px ${magnifierPosition.value.y}px`
}))

// Handles mouse entering the main image area
const handleMouseEnter = () => {
  if (!currentImage.value) return
  isMouseInImage = true
  // Preload the image to ensure it's ready for magnification
  const img = new Image()
  img.src = currentImage.value
  img.onload = () => {
    if (isMouseInImage) { // Only show magnifier if mouse is still in image
      showMagnifier.value = true
    }
  }
}

// Handles mouse leaving the main image area
const handleMouseLeave = () => {
  isMouseInImage = false
  showMagnifier.value = false
}

// Handles mouse movement over the image for magnifier calculations
const handleMouseMove = throttle((event) => {
  if (!isMouseInImage || !currentImage.value || !mainImageRef.value) {
    showMagnifier.value = false
    return
  }

  try {
    const imgElement = mainImageRef.value.querySelector('img')
    if (!imgElement) return

    const { left, top, width, height } = imgElement.getBoundingClientRect()
    // Mouse position relative to the image element
    const x = event.clientX - left
    const y = event.clientY - top

    // If mouse goes out of bounds of the actual image
    if (x < 0 || x > width || y < 0 || y > height) {
      showMagnifier.value = false
      return
    }

    showMagnifier.value = true

    // Calculate lens position: centered on mouse, clamped within image boundaries
    const lensX = Math.min(Math.max(0, x - LENS_SIZE / 2), width - LENS_SIZE)
    const lensY = Math.min(Math.max(0, y - LENS_SIZE / 2), height - LENS_SIZE)

    lensPosition.value = {
      x: lensX,
      y: lensY
    }

    // --- Refined Magnifier Background Position Calculation ---
    // We want the point (x, y) on the original image to be at the center
    // of the magnified view within the magnifier preview box.
    const bgX = MAGNIFIER_WIDTH / 2 - x * MAGNIFICATION_SCALE;
    const bgY = MAGNIFIER_HEIGHT / 2 - y * MAGNIFICATION_SCALE;

    // Limit background position to prevent displaying blank areas
    // The maximum possible value for bgX is 0 (when the left edge of magnified image aligns with magnifier's left edge).
    // The minimum possible value for bgX is -(magnified image width - magnifier width).
    const maxBgX = -(width * MAGNIFICATION_SCALE - MAGNIFIER_WIDTH);
    const maxBgY = -(height * MAGNIFICATION_SCALE - MAGNIFIER_HEIGHT);

    magnifierPosition.value = {
      x: Math.min(0, Math.max(bgX, maxBgX)),
      y: Math.min(0, Math.max(bgY, maxBgY))
    };

    // Update magnifier preview box position relative to the viewport
    const previewElement = document.querySelector('.magnifier-preview')
    if (previewElement) {
      const previewWidth = MAGNIFIER_WIDTH
      const previewHeight = MAGNIFIER_HEIGHT
      const margin = 20 // Margin from the mouse pointer

      // Calculate preview box position, trying to place it to the right of the mouse
      let previewLeft = event.clientX + margin
      let previewTop = event.clientY - previewHeight / 2 // Center vertically with mouse

      // Adjust if preview box goes off the right edge of the window
      if (previewLeft + previewWidth > window.innerWidth) {
        // Place it to the left of the mouse instead
        previewLeft = event.clientX - previewWidth - margin
      }

      // Adjust if preview box goes off the top edge of the window
      if (previewTop < 0) {
        previewTop = 0
      }
      // Adjust if preview box goes off the bottom edge of the window
      else if (previewTop + previewHeight > window.innerHeight) {
        previewTop = window.innerHeight - previewHeight
      }

      previewElement.style.left = `${previewLeft}px`
      previewElement.style.top = `${previewTop}px`
    }
  } catch (error) {
    console.error('Error in handleMouseMove:', error)
    showMagnifier.value = false
  }
}, 16) // Throttled to roughly 60 FPS

// Handles clicking on a thumbnail image
const handleThumbnailClick = (index) => {
  currentImageIndex.value = index
}

// Handles hovering over a thumbnail image (for quick preview)
const handleThumbnailHover = (index) => {
  currentImageIndex.value = index
}

// Generates an object of available specifications for the product
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

// Checks if a specific specification option is available given current selections
const isSpecAvailable = (specName, option) => {
  if (!product.value?.skus) return true

  const tempSpecs = { ...selectedSpecs.value, [specName]: option }

  return product.value.skus.some(sku => {
    const skuSpecs = sku.attributes || sku.specs || {}
    return Object.entries(tempSpecs).every(([key, value]) => {
      // If a value is selected, it must match the SKU's spec value
      // If value is null/undefined (spec not selected yet), it always matches
      return !value || skuSpecs[key] === value
    })
  })
}

// Handles changes in selected product specifications
const handleSpecChange = () => {
  if (!product.value?.skus) {
    selectedSku.value = null;
    return;
  }

  // Find the SKU that matches all currently selected specifications
  selectedSku.value = product.value.skus.find(sku => {
    const skuSpecs = sku.attributes || sku.specs || {}
    return Object.entries(selectedSpecs.value).every(([key, value]) => {
      return !value || skuSpecs[key] === value
    })
  })

  // Optional: Update main image to SKU-specific image if available
  if (selectedSku.value?.image) {
    const imageIndex = product.value.images.indexOf(selectedSku.value.image);
    if (imageIndex !== -1) {
      currentImageIndex.value = imageIndex;
    }
  }
}

// Adds the selected product (and SKU) to the cart
const addToCart = () => {
  if (!product.value) return

  // 检查用户是否已登录
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // Require SKU selection if product has SKUs
  if (product.value.skus && !selectedSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }

  const productId = product.value.productId || product.value.id
  const skuId = selectedSku.value ? selectedSku.value.skuId : null

  cartStore.addItem(productId, skuId, quantity.value)
  ElMessage.success('已添加到购物车')
}

// Initiates the purchase process for the selected product (and SKU)
const buyNow = () => {
  if (!product.value) return

  // Require SKU selection if product has SKUs
  if (product.value.skus && !selectedSku.value) {
    ElMessage.warning('请选择商品规格')
    return
  }

  const productId = product.value.productId || product.value.id
  const productImage = product.value.images?.[0] || '' // Use first image as default
  const skuId = selectedSku.value ? selectedSku.value.skuId : null

  const checkoutItem = {
    id: productId,
    productId: productId,
    skuId: skuId,
    name: product.value.name,
    price: selectedSku.value ? selectedSku.value.price : product.value.price, // Use SKU price if available
    image: productImage,
    quantity: quantity.value,
    specs: selectedSku.value ? selectedSku.value.attributes : null // Include selected SKU specs
  }

  // Navigate to checkout page with item details
  router.push({
    name: 'checkout',
    query: {
      items: JSON.stringify([checkoutItem])
    }
  })
}

// Toggles product favorite status
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  ElMessage.success(isFavorite.value ? '已添加到收藏' : '已取消收藏')
}

// Global mouse move handler to hide magnifier if mouse moves off the main image area
const handleGlobalMouseMove = throttle((event) => {
  // Only act if magnifier is shown and we have a reference to the main image
  if (!showMagnifier.value || !mainImageRef.value || !isMouseInImage) return

  const imageRect = mainImageRef.value.getBoundingClientRect()
  const { clientX, clientY } = event

  // If the mouse pointer is outside the main image's bounding rectangle
  if (
    clientX < imageRect.left ||
    clientX > imageRect.right ||
    clientY < imageRect.top ||
    clientY > imageRect.bottom
  ) {
    isMouseInImage = false // Reset mouse-in-image flag
    showMagnifier.value = false // Hide magnifier
  }
}, 16)

// Lifecycle hook: on component mount
onMounted(() => {
  const productId = route.params.id
  if (productId) {
    productStore.fetchProductDetail(productId).then(() => {
      // 商品详情请求完成后输出一次
      console.log('🟢 商品详情product:', product.value)
      console.log('🟢 商品详情skus:', product.value?.skus)
    })
  }

  // Add global mouse move listener
  window.addEventListener('mousemove', handleGlobalMouseMove)
})

// Lifecycle hook: before component unmount
onBeforeUnmount(() => {
  productStore.clearCurrentProduct() // Clear product data from store
  // Remove global mouse move listener to prevent memory leaks
  window.removeEventListener('mousemove', handleGlobalMouseMove)
})

// Watch for changes in the current image; hide magnifier when image changes
watch(currentImage, () => {
  showMagnifier.value = false
})

// Watch for changes in selected specifications to update the selected SKU
watch(selectedSpecs, handleSpecChange, { deep: true });

// 监听product变化，输出到控制台
watch(product, (val) => {
  console.log('🔵 watch product:', val)
  console.log('🔵 watch skus:', val?.skus)
}, { immediate: true, deep: true })

// 调试输出sku相关数据
watch([product, selectedSku], ([p, sku]) => {
  console.log('【调试】product.skus:', p?.skus)
  console.log('【调试】selectedSku:', sku)
  console.log('【调试】product.price:', p?.price)
  console.log('【调试】product.originalPrice:', p?.originalPrice)
}, { immediate: true, deep: true })

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.product-detail-container {
  max-width: 1400px;
  margin: 20px auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.product-breadcrumb {
  margin-bottom: 25px;
  padding: 10px 0;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.8);
}

.product-content {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.product-images {
  width: 500px;
  flex-shrink: 0;
}

.image-container {
  position: relative;
}

.main-image-container {
  position: relative;
  width: 100%;
  height: 500px;
  border-radius: 16px;
  overflow: visible;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 20px;
  z-index: 1;
}

.main-image {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.main-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.main-image :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.magnifier-lens {
  position: absolute;
  width: 100px;
  height: 100px;
  border: 2px solid #fff;
  border-radius: 50%;
  pointer-events: none;
  background-color: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
  z-index: 2;
}

.magnifier-preview {
  position: fixed;
  width: 400px;
  height: 400px;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  z-index: 9999;
}

.magnifier-image {
  width: 100%;
  height: 100%;
  background-repeat: no-repeat;
  background-size: 300% 300%;
}

.thumbnail-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 5px;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.3) transparent;
}

.thumbnail-list::-webkit-scrollbar {
  height: 6px;
}

.thumbnail-list::-webkit-scrollbar-track {
  background: transparent;
}

.thumbnail-list::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

.thumbnail-item:hover {
  transform: translateY(-2px);
  border-color: rgba(255, 255, 255, 0.3);
}

.thumbnail-item.active {
  border-color: #409EFF;
}

.thumbnail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.thumbnail-item:hover .thumbnail-image {
  transform: scale(1.1);
}

.product-info {
  flex: 1;
  padding: 30px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-info:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.product-name {
  font-size: 28px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 15px;
  line-height: 1.4;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.sales-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.rating-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-info :deep(.el-rate) {
  height: 20px;
  line-height: 20px;
}

.rating-info :deep(.el-rate__icon) {
  font-size: 18px;
  margin-right: 2px;
}

.rating-info :deep(.el-rate__text) {
  font-size: 14px;
  margin-left: 8px;
}

.price-section {
  margin: 25px 0;
  padding: 20px 0 10px 0;
  background: none;
  border-radius: 12px;
  border: none;
}

.current-price {
  font-size: 0;
  margin-bottom: 8px;
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.current-price .label {
  font-size: 16px;
  color: #fff;
  margin-right: 10px;
  font-weight: 500;
}

.price {
  font-size: 32px;
  font-weight: bold;
  color: #ff3b30;
  letter-spacing: 1px;
  margin-right: 10px;
  text-shadow: 0 2px 8px rgba(255,59,48,0.08);
  vertical-align: middle;
  line-height: 1;
}

.price-placeholder {
  font-size: 20px;
  color: #ff9800;
  font-weight: 500;
  margin-left: 8px;
  letter-spacing: 0.5px;
  vertical-align: middle;
}

.original-price {
  font-size: 18px;
  color: #b0b0b0;
  text-decoration: line-through;
  margin-left: 8px;
  vertical-align: middle;
}

.discount {
  margin-top: 4px;
}

.discount-tag {
  display: inline-block;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  margin-left: 10px;
}

.product-description {
  margin: 25px 0;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.8;
  font-size: 15px;
}

.product-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.quantity-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 20px 0;
}

.quantity-wrapper {
  display: flex;
  align-items: center;
  gap: 15px;
}

.quantity-selector {
  width: 150px;
}

.custom-input-number :deep(.el-input-number__decrease),
.custom-input-number :deep(.el-input-number__increase) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

.custom-input-number :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  box-shadow: none;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.custom-input-number :deep(.el-input__inner) {
  color: #fff;
  text-align: center;
}

.custom-input-number :deep(.el-input-number__decrease:hover),
.custom-input-number :deep(.el-input-number__increase:hover) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.stock {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  margin-left: 15px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  align-items: center;
  margin-top: 30px;
}

.main-actions {
  display: flex;
  gap: 15px;
  flex: 1;
}

.action-btn {
  flex: 1;
  height: 45px;
  font-size: 16px;
}

.favorite-btn {
  width: 45px !important;
  height: 45px !important;
  padding: 0 !important;
  font-size: 20px;
}

.favorite-btn :deep(.el-icon) {
  font-size: 20px;
}

.product-tabs {
  margin-top: 30px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-tabs__nav) {
  background: rgba(255, 255, 255, 0.05);
}

:deep(.el-tabs__item) {
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  padding: 0 25px;
  height: 50px;
  line-height: 50px;
  transition: all 0.3s ease;
}

:deep(.el-tabs__item.is-active) {
  color: #fff;
  font-weight: 600;
}

:deep(.el-tabs__item:hover) {
  color: #fff;
}

:deep(.el-tabs__active-bar) {
  background-color: #409EFF;
  height: 3px;
  border-radius: 3px;
}

.tab-content {
  padding: 30px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.8;
}

.specifications-table {
  width: 100%;
  border-collapse: collapse;
}

.specifications-table th,
.specifications-table td {
  padding: 12px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  text-align: left;
}

.specifications-table th {
  font-weight: 600;
  color: #fff;
  background: rgba(255, 255, 255, 0.05);
}

.specifications-table tr:last-child td {
  border-bottom: none;
}

@media (max-width: 1200px) {
  .product-content {
    flex-direction: column;
  }
  
  .product-images {
    width: 100%;
  }
  
  .main-image-container {
    height: 400px;
  }
}

@media (max-width: 768px) {
  .product-detail-container {
    padding: 15px;
  }
  
  .main-image-container {
    height: 300px;
  }
  
  .product-name {
    font-size: 24px;
  }
  
  .current-price {
    font-size: 28px;
  }
  
  .product-actions {
    flex-direction: column;
  }
  
  .quantity-selector {
    width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .product-detail-container {
    padding: 10px;
  }
  
  .main-image-container {
    height: 250px;
  }
  
  .product-name {
    font-size: 20px;
  }
  
  .current-price {
    font-size: 24px;
  }
  
  .product-meta {
    gap: 10px;
  }
  
  .product-brand {
    font-size: 12px;
  }
  
  .product-sales {
    font-size: 12px;
  }
  
  .tab-content {
    padding: 20px;
  }
  
  .specifications-table th,
  .specifications-table td {
    padding: 8px 12px;
    font-size: 14px;
  }
}

.back-btn {
  margin-bottom: 18px;
  background: rgba(255,255,255,0.08) !important;
  border: 1px solid rgba(255,255,255,0.18) !important;
  color: #fff !important;
  font-size: 16px !important;
  font-weight: 500;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: background 0.2s, color 0.2s;
}
.back-btn:hover {
  background: rgba(64,158,255,0.18) !important;
  color: #409EFF !important;
}
</style>
