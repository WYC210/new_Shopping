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
            <div class="category-info">
              <div class="category-icon" :class="'category-icon-' + (category.categoryId % 5)">
                <el-icon><component :is="getCategoryIcon(category.name)" /></el-icon>
              </div>
              <h3>{{ category.name }}</h3>
              <div class="category-divider" :class="'category-divider-' + (category.categoryId % 5)"></div>
              <p>{{ category.description || '探索更多精彩商品' }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <!-- 优惠券区域 -->
    <section class="section coupons-section">
      <h2 class="section-title">领取优惠券</h2>
      <div class="coupon-loading" v-if="availableCoupons.length === 0 && couponsLoading">
        <el-skeleton :rows="3" animated />
      </div>
      <div class="empty-coupon" v-else-if="availableCoupons.length === 0 && !couponsLoading">
        <el-empty description="暂无可用优惠券" />
      </div>
      <el-scrollbar class="coupon-scrollbar" v-else>
        <div class="coupons-container">
          <div class="coupon-card" v-for="coupon in availableCoupons" :key="coupon.couponId">
            <div class="coupon-content" :class="'coupon-type-' + (coupon.couponType)">
              <div class="coupon-left">
                <div class="coupon-value">
                  <template v-if="coupon.couponType === 'fixed'">
                    <span class="coupon-currency">¥</span>
                    <span class="coupon-amount">{{ coupon.value }}</span>
                  </template>
                  <template v-else-if="coupon.couponType === 'percentage'">
                    <span class="coupon-amount">{{ (coupon.discountPercentage / 10).toFixed(1) }}</span>
                    <span class="coupon-unit">折</span>
                  </template>
                  <template v-else-if="coupon.couponType === 'shipping'">
                    <span class="coupon-amount">免运费</span>
                  </template>
                  <template v-else>
                    <span class="coupon-amount">其他</span>
                  </template>
                </div>
                <div class="coupon-threshold" v-if="coupon.threshold > 0">
                  <template v-if="coupon.couponType === 'shipping'">
                    满{{ coupon.threshold }}元包邮
                  </template>
                  <template v-else>
                    满{{ coupon.threshold }}元可用
                  </template>
                </div>
                <div class="coupon-scope" v-if="coupon.applicableScope && coupon.applicableScope !== 'all'">
                  <span>适用范围：{{ coupon.applicableScope === 'category' ? '指定品类' : coupon.applicableScope }}</span>
                </div>
              </div>
              <div class="coupon-divider"></div>
              <div class="coupon-right">
                <h4 class="coupon-name">{{ coupon.name }}</h4>
                <p class="coupon-desc">{{ coupon.description || '全场通用' }}</p>
                <p class="coupon-time">{{ formatCouponDate(coupon.startTime) }} 至 {{ formatCouponDate(coupon.endTime) }}</p>
                <el-button 
                  size="small" 
                  class="coupon-btn" 
                  :class="{'coupon-btn-received': coupon.received}"
                  :loading="coupon.receiving"
                  :disabled="coupon.received"
                  @click.stop="receiveCoupon(coupon)"
                >
                  {{ coupon.received ? '已领取' : '立即领取' }}
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </section>

    <section class="section featured-section">
      <h2 class="section-title">精选商品</h2>
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in featuredProducts" :key="product.productId">
          <el-card shadow="hover" class="product-card" @click="navigateToProduct(product.productId)">
            <div class="product-image-wrapper">
              <el-image 
                :src="product.mainImageUrl" 
                fit="cover" 
                loading="lazy"
                @error="handleImageError($event, product)"
              >
                <template #error>
                  <div class="image-error-fallback">
                    <el-icon><Picture /></el-icon>
                    <span>图片加载失败</span>
                  </div>
                </template>
              </el-image>
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
              <el-image 
                :src="product.mainImageUrl" 
                fit="cover" 
                loading="lazy"
                @error="handleImageError($event, product)"
              >
                <template #error>
                  <div class="image-error-fallback">
                    <el-icon><Picture /></el-icon>
                    <span>图片加载失败</span>
                  </div>
                </template>
              </el-image>
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
import { useUserStore } from '../stores/user';
import { ElMessage } from 'element-plus';
import { ShoppingCart, Picture, ShoppingBag, Goods, Present, Food, Reading, Shop, PhoneFilled, OfficeBuilding, Star } from '@element-plus/icons-vue';
import apiClient from '../api/client';

const router = useRouter();
const cartStore = useCartStore();
const productStore = useProductStore();
const userStore = useUserStore();

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
const categories = computed(() => {
  // 确保分类数据中有描述，如果没有则添加默认描述
  return (productStore.categories || []).map(cat => ({
    ...cat,
    description: cat.description || '探索精选商品系列'
  }));
});
const featuredProducts = computed(() => productStore.featuredProducts);
const newArrivals = computed(() => productStore.newArrivals);

// 优惠券数据
const availableCoupons = ref([]);
const couponsLoading = ref(false);

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
  
  // 检查用户是否已登录
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录');
    router.push('/login');
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
  console.log('🏠 开始加载首页数据')
  try {
    console.log('📦 开始获取商品分类')
    await productStore.fetchCategories()
    console.log('📦 商品分类加载完成')
    
    console.log('🔥 开始获取热门商品')
    await productStore.fetchHotProducts()
    console.log('🔥 热门商品加载完成')
    
    console.log('🆕 开始获取新品')
    await productStore.fetchNewProducts()
    console.log('🆕 新品加载完成')
    
    console.log('🎟️ 开始获取优惠券')
    await fetchAvailableCoupons()
    console.log('🎟️ 优惠券加载完成')
    
    console.log('✅ 首页数据加载完成')
  } catch (error) {
    console.error('❌ 首页数据加载失败:', error)
    ElMessage.error('数据加载失败，请刷新页面重试')
  }
});

// --- 优惠券相关函数 ---
const fetchAvailableCoupons = async () => {

  couponsLoading.value = true;
  try {
    const response = await apiClient.get('/coupons/available');

    // 确保正确处理API响应
    if (response.data && response.code === 200) {
      availableCoupons.value = response.data.map(coupon => ({
        couponId: coupon.couponId,
        name: coupon.name,
        value: coupon.value,
        threshold: coupon.threshold,
        description: coupon.description,
        startTime: coupon.startTime,
        endTime: coupon.endTime,
        // 其他需要的字段
        received: false,
        receiving: false
      }));
    } else {
      console.error('获取优惠券失败:', response.data?.msg);
    }
  } catch (error) {
    console.error('获取优惠券异常:', error);
    console.error('错误响应:', error.response); // 打印错误响应
    ElMessage.error('获取优惠券失败，请稍后再试');
  } finally {
    couponsLoading.value = false;
  }
};

const receiveCoupon = async (coupon) => {
  // 检查用户是否已登录
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录后才能领取优惠券');
    router.push('/login');
    return;
  }
  // 防止重复点击
  if (coupon.receiving) return;
  coupon.receiving = true;
  try {
    // 调用后端API领取优惠券
    const response = await apiClient.post(`/coupons/${coupon.couponId}/receive`);
    // 只要没有异常就认为领取成功
    coupon.received = true;
    ElMessage.success('优惠券领取成功');
  } catch (error) {
    console.error('领取优惠券失败:', error);
    if (error.response) {
      if (error.response.status === 403) {
        ElMessage.warning('您需要登录后才能领取优惠券');
        router.push('/login');
      } else if (error.response.data && error.response.data.msg) {
        ElMessage.error(error.response.data.msg);
      } else {
        ElMessage.error('领取失败，请稍后再试');
      }
    } else {
      ElMessage.error('网络异常，请稍后再试');
    }
  } finally {
    coupon.receiving = false;
  }
};

const formatCouponDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getMonth() + 1}.${date.getDate()}`;
};

// --- Image Error Handling ---
const defaultImage = 'https://via.placeholder.com/300x300?text=ShopSphere';

const handleImageError = (event, product) => {
  console.error(`图片加载失败: ${product.name}`, event);
  // 设置默认图片
  product.mainImageUrl = defaultImage;
};

// --- Category Icon Functions ---
const getCategoryIcon = (categoryName) => {
  if (!categoryName) return 'ShoppingBag';
  
  const name = categoryName.toLowerCase();
  
  if (name.includes('手机') || name.includes('电子') || name.includes('数码'))
    return 'PhoneFilled';
  if (name.includes('服装') || name.includes('鞋') || name.includes('包'))
    return 'ShoppingBag';
  if (name.includes('食品') || name.includes('生鲜') || name.includes('零食'))
    return 'Food';
  if (name.includes('书') || name.includes('文具') || name.includes('读物'))
    return 'Reading';
  if (name.includes('美妆') || name.includes('护理') || name.includes('个护'))
    return 'Present';
  if (name.includes('家居') || name.includes('家电') || name.includes('家装'))
    return 'OfficeBuilding';
  
  // 默认图标
  return 'Goods';
};
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
  min-height: 200px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.category-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.15), rgba(255, 255, 255, 0.05));
}

.category-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.category-icon-0 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 8px 15px rgba(79, 172, 254, 0.3);
}

.category-icon-1 {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  box-shadow: 0 8px 15px rgba(255, 154, 158, 0.3);
}

.category-icon-2 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 8px 15px rgba(102, 126, 234, 0.3);
}

.category-icon-3 {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  box-shadow: 0 8px 15px rgba(67, 233, 123, 0.3);
}

.category-icon-4 {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  box-shadow: 0 8px 15px rgba(250, 112, 154, 0.3);
}

.category-divider-0 {
  background: linear-gradient(90deg, #4facfe, #00f2fe);
}

.category-divider-1 {
  background: linear-gradient(90deg, #ff9a9e, #fad0c4);
}

.category-divider-2 {
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.category-divider-3 {
  background: linear-gradient(90deg, #43e97b, #38f9d7);
}

.category-divider-4 {
  background: linear-gradient(90deg, #fa709a, #fee140);
}

.category-info {
  padding: 30px 20px;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  transition: all 0.3s ease;
}

.category-info h3 {
  margin: 0 0 12px;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.category-info p {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  line-height: 1.5;
}

/* --- Product Card Styles --- */
.product-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  overflow: hidden;
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

.product-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%; /* 强制1:1比例 */
  overflow: hidden;
}

.product-image-wrapper .el-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover .product-image-wrapper .el-image {
  transform: scale(1.1);
}

.image-error-fallback {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.6);
}

.image-error-fallback .el-icon {
  font-size: 40px;
  margin-bottom: 10px;
}

.image-error-fallback span {
  font-size: 14px;
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
  .category-icon {
    width: 50px;
    height: 50px;
  }
  
  .category-icon .el-icon {
    font-size: 24px;
  }
  
  .category-info h3 {
    font-size: 18px;
  }
  
  .category-info p {
    font-size: 13px;
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
  .category-card {
    min-height: 160px;
  }
  
  .category-info {
    padding: 20px 15px;
  }
  
  .category-icon {
    width: 45px;
    height: 45px;
    margin-bottom: 10px;
  }
  
  .category-icon .el-icon {
    font-size: 20px;
  }
  
  .category-divider {
    margin: 8px auto 10px;
  }
  
  .category-info h3 {
    font-size: 16px;
    margin-bottom: 8px;
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

.category-icon .el-icon {
  font-size: 28px;
  color: white;
}

.category-divider {
  width: 40px;
  height: 3px;
  margin: 12px auto 15px;
  border-radius: 3px;
  transition: all 0.3s ease;
}

.category-card:hover .category-icon {
  transform: scale(1.1) translateY(-5px);
}

.category-card:hover .category-divider {
  width: 60px;
}

/* --- Coupon Section Styles --- */
.coupons-section {
  margin-bottom: 40px;
}

.coupon-scrollbar {
  width: 100%;
  height: auto;
}

.coupons-container {
  display: flex;
  flex-wrap: nowrap;
  gap: 15px;
  padding: 10px 0;
  overflow-x: auto;
}

.coupon-card {
  flex: 0 0 auto;
  width: 320px;
  transition: transform 0.3s ease;
}

.coupon-card:hover {
  transform: translateY(-5px);
}

.coupon-content {
  display: flex;
  height: 130px;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.coupon-type-0 {
  background: linear-gradient(135deg, rgba(79, 172, 254, 0.2), rgba(0, 242, 254, 0.2));
  border: 1px solid rgba(79, 172, 254, 0.3);
}

.coupon-type-1 {
  background: linear-gradient(135deg, rgba(255, 154, 158, 0.2), rgba(250, 208, 196, 0.2));
  border: 1px solid rgba(255, 154, 158, 0.3);
}

.coupon-type-2 {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2), rgba(118, 75, 162, 0.2));
  border: 1px solid rgba(102, 126, 234, 0.3);
}

.coupon-type-3 {
  background: linear-gradient(135deg, rgba(67, 233, 123, 0.2), rgba(56, 249, 215, 0.2));
  border: 1px solid rgba(67, 233, 123, 0.3);
}

.coupon-type-4 {
  background: linear-gradient(135deg, rgba(250, 112, 154, 0.2), rgba(254, 225, 64, 0.2));
  border: 1px solid rgba(250, 112, 154, 0.3);
}

.coupon-left {
  width: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 15px;
  position: relative;
}

.coupon-divider {
  position: relative;
  width: 1px;
  background: rgba(255, 255, 255, 0.3);
}

.coupon-divider::before,
.coupon-divider::after {
  content: '';
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 50%;
}

.coupon-divider::before {
  top: -10px;
}

.coupon-divider::after {
  bottom: -10px;
}

.coupon-right {
  flex: 1;
  padding: 15px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.coupon-value {
  display: flex;
  align-items: baseline;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.coupon-currency {
  font-size: 18px;
  font-weight: bold;
  margin-right: 2px;
}

.coupon-amount {
  font-size: 36px;
  font-weight: 900;
  line-height: 1;
}

.coupon-unit {
  font-size: 18px;
  font-weight: bold;
  margin-left: 2px;
}

.coupon-threshold {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 5px;
}

.coupon-name {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 8px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.coupon-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.coupon-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 10px;
}

.coupon-btn {
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  transition: all 0.3s ease;
}

.coupon-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.coupon-btn-received {
  background: rgba(255, 255, 255, 0.1);
  cursor: not-allowed;
}

.coupon-loading,
.empty-coupon {
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  margin-bottom: 20px;
}
</style>