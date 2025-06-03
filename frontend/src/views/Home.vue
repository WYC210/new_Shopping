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
  max-width: 1200px; /* Limit width for better readability on large screens */
  margin: 0 auto;
  padding: 20px;
  background-color: #f8f9fa; /* Lighter background */
}

/* --- Section Styling --- */
.section {
  margin-bottom: 40px;
  padding: 30px; /* Slightly more padding */
  background: white;
  border-radius: 12px; /* More rounded corners */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); /* Softer, larger shadow */
  transition: all 0.3s ease;
}

.section-title {
  font-size: 28px; /* Slightly larger title */
  color: #333; /* Darker text */
  margin-bottom: 25px; /* More space below title */
  padding-bottom: 15px;
  border-bottom: 3px solid #409EFF; /* Thicker border */
  text-align: center; /* Center the title */
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -3px; /* Align with border-bottom */
  left: 50%;
  transform: translateX(-50%);
  width: 80px; /* Short underline */
  height: 3px;
  background-color: #36D1DC; /* Complementary color */
}

/* --- Banner Section Styles --- */
.banner-section {
  margin-bottom: 40px;
}

.banner-content {
  height: 100%;
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: flex-start; /* Align text to the left */
  padding: 0 8%; /* Adjust padding */
  text-align: left;
}

.banner-text {
  color: white;
  text-shadow: 0 3px 6px rgba(0, 0, 0, 0.4); /* Stronger shadow for text */
  max-width: 600px; /* Limit text width */
}

.banner-text h2 {
  font-size: 42px; /* Larger banner title */
  margin-bottom: 18px;
  line-height: 1.2;
}

.banner-text p {
  font-size: 20px; /* Larger banner description */
  margin-bottom: 30px;
  line-height: 1.5;
}

/* --- Category Card Styles --- */
.category-card {
  cursor: pointer;
  border-radius: 10px; /* More rounded */
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  margin-bottom: 20px; /* Add margin-bottom for grid spacing */
}

.category-card:hover {
  transform: translateY(-8px); /* More pronounced hover effect */
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15); /* Stronger shadow on hover */
}

.category-card .el-image {
  width: 100%;
  height: 220px; /* Slightly taller images */
  display: block; /* Remove extra space below image */
}

.category-info {
  padding: 18px; /* More padding */
  background: white;
  text-align: center; /* Center category info */
}

.category-info h3 {
  margin: 0 0 10px;
  color: #303133;
  font-size: 18px;
}

.category-info p {
  margin: 0;
  color: #606266; /* Slightly darker grey for readability */
  font-size: 14px;
}

/* --- Product Card Styles --- */
.product-card {
  cursor: pointer;
  border-radius: 10px; /* More rounded */
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  background: white;
  margin-bottom: 20px; /* Add margin-bottom for grid spacing */
}

.product-card:hover {
  transform: translateY(-8px); /* More pronounced hover effect */
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15); /* Stronger shadow on hover */
}

.product-image-wrapper {
  position: relative;
  height: 200px; /* Consistent image height */
  overflow: hidden; /* Ensure tags/badges stay within bounds */
}

.product-image-wrapper .el-image {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image-wrapper .el-image {
  transform: scale(1.05); /* Slight zoom on hover */
}

.product-tags {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  gap: 8px;
  z-index: 2; /* Ensure tags are above image */
}

.new-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #67C23A; /* Success green for new */
  color: white;
  padding: 5px 10px;
  border-radius: 20px; /* Pill shape */
  font-size: 13px;
  font-weight: bold;
  z-index: 2;
}

.product-info {
  padding: 18px;
}

.product-info h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #303133;
  white-space: nowrap; /* Prevent line breaks */
  overflow: hidden; /* Hide overflow */
  text-overflow: ellipsis; /* Add ellipsis */
}

.product-description {
  margin: 0 0 12px; /* More space below description */
  color: #909399;
  font-size: 14px;
  height: 40px; /* Keep consistent height */
  line-height: 20px; /* Ensure consistent line height */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin-bottom: 12px; /* More space below price */
  display: flex;
  align-items: baseline;
}

.current-price {
  color: #F56C6C; /* Element Plus danger color */
  font-size: 22px; /* Larger price */
  font-weight: bold;
  margin-right: 8px;
}

.original-price {
  color: #C0C4CC; /* Lighter grey for original price */
  font-size: 15px;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px; /* Space above meta info */
}

.sales {
  color: #909399;
  font-size: 14px;
}

/* --- Element Plus Button Customization --- */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409EFF 0%, #36D1DC 100%); /* Blue to Cyan gradient */
  border: none;
  font-weight: bold;
  letter-spacing: 0.5px;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #36D1DC 0%, #409EFF 100%);
  transform: translateY(-2px); /* More subtle lift */
  box-shadow: 0 6px 15px rgba(64, 158, 255, 0.3); /* Clearer shadow */
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
    width: 33.33333%; /* Adjust for 3 columns on medium screens */
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
    max-width: 80%; /* Allow text to take more width */
  }
  .banner-text h2 {
    font-size: 30px;
  }
  .banner-text p {
    font-size: 16px;
  }
  .el-col-md-8 {
    width: 50%; /* Adjust for 2 columns on small screens */
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
    height: 300px !important; /* Smaller banner on extra small screens */
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
    width: 100%; /* Full width on extra small screens */
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