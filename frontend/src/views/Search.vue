<template>
  <div class="search-container content-container">
    <div class="search-header">
      <div class="search-title">
        <el-icon><Search /></el-icon>
        <span>搜索结果</span>
      </div>
      <div class="search-stats" v-if="!loading">
        找到 {{ totalProducts }} 个相关商品
      </div>
    </div>

    <div class="search-content">
      <div class="filter-sidebar">
        <div class="filter-card">
          <div class="filter-section">
            <div class="section-title">价格区间</div>
            <div class="price-range">
              <div class="price-input">
                <el-input v-model="filters.minPrice" placeholder="最低价" type="number" />
              </div>
              <span class="price-separator">-</span>
              <div class="price-input">
                <el-input v-model="filters.maxPrice" placeholder="最高价" type="number" />
              </div>
            </div>
          </div>

          <div class="filter-section">
            <div class="section-title">品牌</div>
            <div class="brand-list">
              <div
                v-for="brand in brands"
                :key="brand.id"
                class="brand-item"
                @click="toggleBrand(brand.id)"
              >
                <el-checkbox v-model="brand.checked" class="brand-checkbox" />
                {{ brand.name }}
              </div>
            </div>
          </div>

          <div class="filter-actions">
            <el-button class="filter-button" @click="applyFilters">应用筛选</el-button>
            <el-button class="filter-button" @click="resetFilters">重置</el-button>
          </div>
        </div>
      </div>

      <div class="search-main">
        <div class="sort-bar">
          <div class="sort-options">
            <div
              v-for="option in sortOptions"
              :key="option.value"
              class="sort-option"
              :class="{ active: currentSort === option.value }"
              @click="changeSort(option.value)"
            >
              {{ option.label }}
            </div>
          </div>
          <div class="view-options">
            <div
              class="view-option"
              :class="{ active: viewMode === 'grid' }"
              @click="viewMode = 'grid'"
            >
              <el-icon><Grid /></el-icon>
            </div>
            <div
              class="view-option"
              :class="{ active: viewMode === 'list' }"
              @click="viewMode = 'list'"
            >
              <el-icon><List /></el-icon>
            </div>
          </div>
        </div>

        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="products.length === 0" class="empty-result">
          <el-empty description="未找到相关商品" />
        </div>
        <div v-else :class="viewMode === 'grid' ? 'product-grid' : 'product-list'">
          <el-card
            v-for="product in products"
            :key="product.productId"
            class="product-card"
            @click="navigateToProduct(product.productId)"
          >
            <div class="product-image">
              <img :src="product.mainImageUrl || defaultImage" @error="handleImageError($event, product)" />
            </div>
            <div class="product-info">
              <div class="product-name">{{ product.name || '未命名商品' }}</div>
              <div class="product-meta">
                <div class="product-brand">{{ product.brand || '无品牌' }}</div>
                <div class="product-rating" v-if="product.rating">
                  <el-icon><Star /></el-icon>
                  {{ product.rating }}
                </div>
              </div>
              <div class="product-description" v-if="product.description">
                {{ product.description }}
              </div>
              <div class="product-price">¥{{ (product.price ?? 0).toFixed(2) }}</div>
              <div class="product-stock">库存: {{ product.stock || 0 }}</div>
              <div class="product-actions">
                <el-button class="action-button" @click.stop="addToCart(product)">
                  <el-icon><ShoppingCart /></el-icon>
                  加入购物车
                </el-button>
              </div>
            </div>
          </el-card>
        </div>

        <div class="pagination-container" v-if="totalPages > 1">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="totalProducts"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, Grid, List, Star, ShoppingCart } from '@element-plus/icons-vue';
import { useCartStore } from '../stores/cart';
import { useUserStore } from '../stores/user';
import { useProductStore } from '../stores/product';
import apiClient from '../api/client';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();
const productStore = useProductStore();

// 状态变量
const loading = ref(false);
const products = ref([]);
const totalProducts = ref(0);
const currentPage = ref(1);
const pageSize = ref(12);
const viewMode = ref('grid');
const currentSort = ref('default');
const defaultImage = 'https://via.placeholder.com/300x300?text=ShopSphere';
const searchQuery = ref('');
const brands = ref([]);
const lastSearchParams = ref(null);

// 计算属性
const totalPages = computed(() => Math.ceil(totalProducts.value / pageSize.value));

// 过滤器和排序选项
const filters = reactive({
  minPrice: '',
  maxPrice: '',
  brands: []
});

const sortOptions = [
  { label: '默认排序', value: 'default' },
  { label: '价格 ↑', value: 'price_asc' },
  { label: '价格 ↓', value: 'price_desc' },
  { label: '最新上架', value: 'newest' },
  { label: '销量优先', value: 'sales' }
];

// 监听路由参数变化
watch(() => route.query, (newQuery) => {
  if (newQuery.q !== searchQuery.value) {
    searchQuery.value = newQuery.q || '';
    resetFilters();
    currentPage.value = 1;
    fetchProducts();
  }
}, { immediate: true, deep: true });

// 初始化
onMounted(async () => {
  searchQuery.value = route.query.q || '';
  await fetchBrands();
  await fetchProducts();
});

// 获取品牌列表
const fetchBrands = async () => {
  try {
    const response = await apiClient.get('/brands');
    if (response && response.code === 200) {
      brands.value = (response.data || []).map(brand => ({
        ...brand,
        checked: false
      }));
    }
  } catch (error) {
    console.error('获取品牌列表失败:', error);
  }
};

// 获取商品列表
const fetchProducts = async () => {
  if (!searchQuery.value) {
    products.value = [];
    totalProducts.value = 0;
    return;
  }

  // 构建搜索参数
  const params = {
    keyword: searchQuery.value,
    page: currentPage.value,
    size: pageSize.value,
    sort: currentSort.value
  };

  // 添加价格筛选
  if (filters.minPrice) params.minPrice = filters.minPrice;
  if (filters.maxPrice) params.maxPrice = filters.maxPrice;

  // 添加品牌筛选
  const selectedBrands = brands.value
    .filter(brand => brand.checked)
    .map(brand => brand.id);
  if (selectedBrands.length > 0) {
    params.brands = selectedBrands.join(',');
  }

  // 检查是否与上次请求参数相同，如果相同则使用缓存
  const paramsString = JSON.stringify(params);
  if (lastSearchParams.value === paramsString) {
    console.log('使用缓存的搜索结果');
    return;
  }

  loading.value = true;
  try {
    const response = await apiClient.get('/products/search', { params });
    console.log('搜索结果:', response);
    
    if (response && response.code === 200) {
      // 直接使用返回的数组数据
      products.value = response.data || [];
      totalProducts.value = products.value.length; // 使用数组长度作为总数
      
      // 缓存请求参数
      lastSearchParams.value = paramsString;
      console.log(`加载了 ${products.value.length} 个商品`);
    } else {
      throw new Error(response?.msg || '搜索失败');
    }
  } catch (error) {
    console.error('搜索商品失败:', error);
    ElMessage.error('搜索商品失败，请重试');
    products.value = [];
    totalProducts.value = 0;
  } finally {
    loading.value = false;
  }
};

// 图片加载错误处理
const handleImageError = (event, product) => {
  product.mainImageUrl = defaultImage;
};

// 添加到购物车
const addToCart = (product) => {
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  if (product.stock === 0) {
    ElMessage.warning('抱歉，该商品已售罄');
    return;
  }

  if (product.hasSkus) {
    ElMessage.info('该商品有多个规格，请前往详情页选择');
    navigateToProduct(product.productId);
    return;
  }

  cartStore.addItem(product.productId, product.defaultSkuId || null, 1);
  ElMessage.success('商品已加入购物车');
};

// 导航到商品详情页
const navigateToProduct = (productId) => {
  router.push({ name: 'product', params: { id: productId } });
};

// 筛选和排序功能
const toggleBrand = (brandId) => {
  const brand = brands.value.find(b => b.id === brandId);
  if (brand) {
    brand.checked = !brand.checked;
  }
};

const applyFilters = () => {
  currentPage.value = 1;
  fetchProducts();
};

const resetFilters = () => {
  filters.minPrice = '';
  filters.maxPrice = '';
  brands.value.forEach(brand => {
    brand.checked = false;
  });
};

const changeSort = (sortValue) => {
  if (currentSort.value === sortValue) return;
  currentSort.value = sortValue;
  currentPage.value = 1;
  fetchProducts();
};

const handlePageChange = (page) => {
  if (currentPage.value === page) return;
  currentPage.value = page;
  fetchProducts();
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' });
};
</script>

<style scoped>
.search-container {
  max-width: 1400px;
  margin: 20px auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.search-header {
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
  animation: fadeIn 0.6s ease-out;
}

.search-header:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.search-title {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-title .el-icon {
  font-size: 28px;
  color: rgba(255, 255, 255, 0.9);
}

.search-stats {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.search-content {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 30px;
}

.filter-sidebar {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.filter-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 25px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeIn 0.6s ease-out;
}

.filter-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.filter-section {
  margin-bottom: 25px;
}

.filter-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.price-range {
  display: flex;
  gap: 10px;
  align-items: center;
}

.price-input {
  flex: 1;
}

.price-input .el-input {
  --el-input-bg-color: rgba(255, 255, 255, 0.1);
  --el-input-border-color: rgba(255, 255, 255, 0.2);
  --el-input-hover-border-color: rgba(255, 255, 255, 0.3);
  --el-input-focus-border-color: rgba(255, 255, 255, 0.4);
  --el-input-text-color: #fff;
  --el-input-placeholder-color: rgba(255, 255, 255, 0.6);
}

.price-input .el-input__wrapper {
  box-shadow: none !important;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.price-input .el-input__wrapper:hover {
  background: rgba(255, 255, 255, 0.15);
}

.price-input .el-input__wrapper.is-focus {
  background: rgba(255, 255, 255, 0.2);
}

.price-separator {
  color: rgba(255, 255, 255, 0.6);
  font-size: 16px;
}

.brand-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 200px;
  overflow-y: auto;
  padding-right: 10px;
}

.brand-list::-webkit-scrollbar {
  width: 4px;
}

.brand-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.brand-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.brand-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
}

.brand-item:hover {
  color: #fff;
  transform: translateX(5px);
}

.brand-checkbox {
  --el-checkbox-checked-bg-color: rgba(255, 255, 255, 0.2);
  --el-checkbox-checked-border-color: rgba(255, 255, 255, 0.3);
}

.filter-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.filter-button {
  flex: 1;
  height: 36px;
  font-size: 14px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  transition: all 0.3s ease;
}

.filter-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.filter-button:active {
  transform: translateY(0);
}

.search-main {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 25px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeIn 0.6s ease-out;
}

.sort-bar:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.sort-options {
  display: flex;
  gap: 15px;
}

.sort-option {
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 5px 10px;
  border-radius: 6px;
}

.sort-option:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.sort-option.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

.view-options {
  display: flex;
  gap: 10px;
}

.view-option {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 6px;
}

.view-option:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.view-option.active {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.product-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fadeIn 0.6s ease-out;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.product-image {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
}

.product-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.4s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #fff;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.product-brand {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.product-rating {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #ffd700;
}

.product-description {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 12px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 8px;
}

.product-stock {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 12px;
}

.product-actions {
  display: flex;
  gap: 10px;
}

.action-button {
  flex: 1;
  height: 36px;
  font-size: 14px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  transition: all 0.3s ease;
}

.action-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-button:active {
  transform: translateY(0);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.pagination-container .el-pagination {
  --el-pagination-bg-color: rgba(255, 255, 255, 0.1);
  --el-pagination-hover-color: #fff;
  --el-pagination-button-color: rgba(255, 255, 255, 0.8);
  --el-pagination-button-bg-color: rgba(255, 255, 255, 0.1);
  --el-pagination-button-disabled-color: rgba(255, 255, 255, 0.4);
  --el-pagination-button-disabled-bg-color: rgba(255, 255, 255, 0.05);
}

.pagination-container .el-pagination .el-pager li {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.pagination-container .el-pagination .el-pager li:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.pagination-container .el-pagination .el-pager li.active {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.4);
}

.loading-container {
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  animation: fadeIn 0.6s ease-out;
}

.empty-result {
  padding: 40px;
  text-align: center;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1200px) {
  .search-content {
    grid-template-columns: 1fr;
  }
  
  .filter-sidebar {
    position: static;
  }
  
  .filter-card {
    margin-bottom: 25px;
  }
}

@media (max-width: 768px) {
  .search-container {
    padding: 15px;
  }
  
  .search-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .sort-bar {
    flex-direction: column;
    gap: 15px;
  }
  
  .sort-options {
    width: 100%;
    justify-content: center;
  }
  
  .view-options {
    width: 100%;
    justify-content: center;
  }
  
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}

@media (max-width: 576px) {
  .search-container {
    padding: 10px;
  }
  
  .search-title {
    font-size: 20px;
  }
  
  .search-stats {
    font-size: 13px;
  }
  
  .section-title {
    font-size: 16px;
  }
  
  .product-name {
    font-size: 15px;
  }
  
  .product-brand {
    font-size: 13px;
  }
  
  .product-price {
    font-size: 16px;
  }
  
  .action-button {
    height: 32px;
    font-size: 13px;
  }
}
</style> 