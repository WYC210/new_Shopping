<template>
  <div class="home-container">
    <section class="banner-section">
      <el-carousel height="500px" :interval="5000" arrow="always" indicator-position="outside">
        <el-carousel-item v-for="banner in banners" :key="banner.id">
          <div class="banner-content" :style="{ backgroundImage: `url(${banner.image})` }">
            <div class="banner-text">
              <h2>{{ banner.title }}</h2>
              <p>{{ banner.description }}</p>
              <el-button type="primary" size="large" @click="navigateToCategory(banner.categoryId)">
                立即查看
              </el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <section class="section categories-section">
      <h2 class="section-title">商品分类</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="category in categories" :key="category.categoryId">
          <el-card shadow="hover" class="category-card" @click="navigateToCategory(category.categoryId)">
            <el-image :src="category.icon" fit="cover" loading="lazy" />
            <div class="category-info">
              <h3>{{ category.name }}</h3>
              <p>{{ category.description }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="section featured-section">
      <h2 class="section-title">精选商品</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in featuredProducts" :key="product.productId">
          <el-card shadow="hover" class="product-card" @click="navigateToProduct(product.productId)">
            <div class="product-image-wrapper">
              <el-image :src="product.mainImageUrl" fit="cover" loading="lazy" />
              <div class="product-tags" v-if="product.tags && product.tags.length">
                <el-tag v-for="tag in product.tags" :key="tag" size="small" effect="dark">
                  {{ tag }}
                </el-tag>
              </div>
            </div>
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="product-description">{{ product.description }}</p>
              <div class="product-price">
                <span class="current-price">¥{{ product.price.toFixed(2) }}</span>
                <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice.toFixed(2) }}</span>
              </div>
              <div class="product-meta">
                <span class="sales">库存: {{ product.stock }}</span>
                <el-button type="primary" size="small" :icon="ShoppingCart" @click.stop="addItemToCart(product)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <section class="section new-arrivals-section">
      <h2 class="section-title">新品上市</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in newArrivals" :key="product.productId">
          <el-card shadow="hover" class="product-card" @click="navigateToProduct(product.productId)">
            <div class="product-image-wrapper">
              <el-image :src="product.mainImageUrl" fit="cover" loading="lazy" />
              <div class="new-badge">新品</div>
            </div>
            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="product-description">{{ product.description }}</p>
              <div class="product-price">
                <span class="current-price">¥{{ product.price.toFixed(2) }}</span>
              </div>
              <div class="product-meta">
                <span class="sales">库存: {{ product.stock }}</span>
                <el-button type="primary" size="small" :icon="ShoppingCart" @click.stop="addItemToCart(product)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '../stores/cart';
import { useProductStore } from '../stores/product';
import { ElMessage } from 'element-plus';
import { ShoppingCart } from '@element-plus/icons-vue';

const router = useRouter();
const cartStore = useCartStore();
const productStore = useProductStore();



// --- Data Definitions ---
const banners = ref([
  {
    id: 1,
    title: '春季新品上市',
    description: '精选优质商品，限时特惠，不容错过！',
    image: 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&h=500',
    categoryId: 1
  },
  {
    id: 2,
    title: '限时折扣季',
    description: '全场商品8折起，优惠多多，赶快抢购！',
    image: 'https://images.unsplash.com/photo-1607082349566-187342175e2f?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&h=500',
    categoryId: 2
  },
  {
    id: 3,
    title: '会员专享福利',
    description: '成为会员，享受更多专属优惠和积分奖励！',
    image: 'https://images.unsplash.com/photo-1607082349566-187342175e2f?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&h=500',
    categoryId: 3
  }
]);

// 使用store中的状态
const categories = computed(() => productStore.categories);
const featuredProducts = computed(() => productStore.featuredProducts);
const newArrivals = computed(() => productStore.newArrivals);

// --- Navigation Functions ---
const navigateToCategory = (categoryId) => {
  router.push({ name: 'category-with-id', params: { id: categoryId } });
};

const navigateToProduct = (productId) => {
  if (!productId) {
    ElMessage.error('商品ID无效');
    return;
  }
  
  // 确保 productId 是字符串类型
  const id = String(productId);
  
  try {
    router.push({
      name: 'product',
      params: { id }
    }).catch(err => {
      if (err.name !== 'NavigationDuplicated') {
        console.error('Navigation error:', err);
        ElMessage.error('页面跳转失败，请稍后重试');
      }
    });
  } catch (error) {
    console.error('Navigation error:', error);
    ElMessage.error('页面跳转失败，请稍后重试');
  }
};

// --- Cart Operations ---
const addItemToCart = (product) => {
  if (product.stock === 0) {
    ElMessage.warning('抱歉，该商品已售罄！');
    return;
  }
  
  // 如果产品有SKU选项但没有选择，则引导用户到商品详情页
  if (product.hasSkus) {
    ElMessage.info('该商品有多个规格，请前往详情页选择');
    navigateToProduct(product.productId);
    return;
  }
  
  const productId = product.productId;
  // 对于主页直接添加的商品，如果没有多规格，使用默认SKU
  const skuId = product.defaultSkuId || null;
  
  cartStore.addItem(productId, skuId, 1);
  ElMessage.success('商品已加入购物车');
};

// --- Lifecycle Hooks ---
onMounted(async () => {
  try {
    await Promise.all([
      productStore.fetchCategories(),
      productStore.fetchHotProducts(),
      productStore.fetchNewProducts()
    ]);
  } catch (error) {
    console.error('Failed to initialize data:', error);
    ElMessage.error('数据加载失败，请刷新页面重试');
  }
});
</script>

<style scoped>
.home-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background: var(--primary-gradient);
  position: relative;
  overflow: hidden;
}

.home-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('/src/assets/pattern.png') repeat;
  opacity: 0.1;
  z-index: 0;
}

/* --- Section Styling --- */
.section {
  margin-bottom: 40px;
  padding: 30px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.section:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.section-title {
  font-size: 28px;
  color: #fff;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
  text-align: center;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 80px;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
}

/* --- Banner Section Styles --- */
.banner-section {
  margin-bottom: 40px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.banner-content {
  height: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0 8%;
  text-align: left;
  position: relative;
}

.banner-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, rgba(0, 0, 0, 0.6) 0%, rgba(0, 0, 0, 0.3) 100%);
  z-index: 1;
}

.banner-text {
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  max-width: 600px;
  position: relative;
  z-index: 2;
}

.banner-text h2 {
  font-size: 42px;
  margin-bottom: 18px;
  line-height: 1.2;
}

.banner-text p {
  font-size: 20px;
  margin-bottom: 30px;
  line-height: 1.5;
}

/* --- Category Card Styles --- */
.category-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.category-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.category-card .el-image {
  width: 100%;
  height: 220px;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.category-card:hover .el-image {
  transform: scale(1.1);
}

.category-info {
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  text-align: center;
}

.category-info h3 {
  margin: 0 0 8px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.category-info p {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

/* --- Product Card Styles --- */
.product-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.product-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.product-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0) 100%);
  opacity: 0;
  transition: opacity 0.4s ease;
}

.product-card:hover::before {
  opacity: 1;
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover .product-image .el-image {
  transform: scale(1.1);
}

.product-tags {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  gap: 8px;
  z-index: 1;
}

.product-tag {
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  color: #333;
  transform: translateY(10px);
  opacity: 0;
  animation: slideUp 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.product-card:hover .product-tag {
  animation: slideDown 0.4s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.product-info {
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

.product-info h3 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #fff;
  font-weight: 600;
  line-height: 1.4;
  height: 44px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-description {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.5;
}

.product-price {
  margin-bottom: 12px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.current-price {
  color: #fff;
  font-size: 22px;
  font-weight: bold;
}

.original-price {
  color: rgba(255, 255, 255, 0.5);
  font-size: 15px;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.sales {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

/* --- Element Plus Button Customization --- */
:deep(.el-button--primary) {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  font-weight: 500;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.el-button--primary:active) {
  transform: translateY(0);
}

/* --- New Badge Styles --- */
.new-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  color: #fff;
  z-index: 1;
}

/* --- Product Tags Styles --- */
.product-tags {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  gap: 8px;
  z-index: 1;
}

:deep(.el-tag) {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
}

/* Responsive Adjustments */
@media (max-width: 992px) {
  .section-title {
    font-size: 26px;
  }
  .banner-text h2 {
    font-size: 36px;
  }
  .banner-text p {
    font-size: 18px;
  }
  .el-col-lg-6 {
    width: 33.33333%;
  }
}

@media (max-width: 768px) {
  .home-container {
    padding: 15px;
  }
  .section {
    padding: 20px;
  }
  .section-title {
    font-size: 24px;
  }
  .banner-content {
    padding: 0 5%;
    text-align: center;
    justify-content: center;
  }
  .banner-text {
    max-width: 80%;
  }
  .banner-text h2 {
    font-size: 30px;
  }
  .banner-text p {
    font-size: 16px;
  }
  .el-col-md-8 {
    width: 50%;
  }
  .category-card .el-image {
    height: 180px;
  }
  .product-image-wrapper {
    height: 180px;
  }
}

@media (max-width: 576px) {
  .home-container {
    padding: 10px;
  }
  .section {
    padding: 15px;
  }
  .section-title {
    font-size: 22px;
  }
  .banner-section .el-carousel {
    height: 300px !important;
  }
  .banner-text h2 {
    font-size: 24px;
  }
  .banner-text p {
    font-size: 14px;
  }
  .banner-text .el-button {
    font-size: 14px;
    padding: 8px 15px;
  }
  .el-col-xs-24 {
    width: 100%;
  }
  .category-card .el-image {
    height: 160px;
  }
  .product-image-wrapper {
    height: 160px;
  }
  .product-info h3 {
    font-size: 16px;
  }
  .current-price {
    font-size: 20px;
  }
}
</style>