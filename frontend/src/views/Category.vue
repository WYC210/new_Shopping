<template>
  <div class="category-container">
    <el-breadcrumb separator="/" class="category-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ currentCategoryName }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="category-content">
      <aside class="category-sidebar">
        <el-card class="sidebar-card">
          <h3>商品分类</h3>
          <div class="category-list">
            <div 
              class="category-item" 
              :class="{ active: activeCategory === '' }"
              @click="handleCategoryClick('')"
            >
              <span>热门商品</span>
            </div>
            <div 
              v-for="(category, index) in categories" 
              :key="index"
              class="category-item"
              :class="{ active: activeCategory === String(getCategoryId(category)) }"
              @click="handleCategoryClick(getCategoryId(category))"
            >
              <span>{{ category.name }}</span>
            </div>
          </div>
        </el-card>

        <el-card class="sidebar-card">
          <h3>价格区间</h3>
          <div class="price-range">
            <el-slider v-model="priceRange" range :min="0" :max="10000" :step="100" @change="applyFilters" />
            <div class="price-inputs">
              <el-input-number v-model="priceRange[0]" :min="0" :max="priceRange[1]" size="small" @change="applyFilters" />
              <span class="price-separator">-</span>
              <el-input-number v-model="priceRange[1]" :min="priceRange[0]" :max="10000" size="small" @change="applyFilters" />
            </div>
          </div>
        </el-card>
      </aside>

      <main class="category-main">
        <el-card class="toolbar-card">
          <div class="toolbar">
            <div class="toolbar-left">
              <el-radio-group v-model="sortBy" size="default" @change="applyFilters">
                <el-radio-button :value="'default'">默认</el-radio-button>
                <el-radio-button :value="'price-asc'">价格从低到高</el-radio-button>
                <el-radio-button :value="'price-desc'">价格从高到低</el-radio-button>
                <el-radio-button :value="'stock-desc'">库存优先</el-radio-button>
              </el-radio-group>
            </div>
            <div class="toolbar-right">
              <span class="total-products-info">共 {{ totalFilteredProducts }} 件商品</span>
              <el-select v-model="pageSize" size="default" @change="handlePageSizeChange">
                <el-option label="20条/页" :value="20" />
                <el-option label="40条/页" :value="40" />
                <el-option label="60条/页" :value="60" />
              </el-select>
            </div>
          </div>
        </el-card>

        <div class="product-grid" v-loading="loadingProducts">
          <el-empty v-if="paginatedProducts.length === 0 && !loadingProducts" description="暂无商品符合条件"></el-empty>
          <div class="product-item" v-for="product in paginatedProducts" :key="product.productId || product.id">
            <el-card shadow="hover" class="product-card" @click="navigateToProduct(product.productId || product.id)">
              <div class="product-image-wrapper">
                <el-image 
                  :src="product.mainImageUrl || product.image" 
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
                  <el-tag v-for="tag in product.tags" :key="tag" size="small" effect="dark" :type="getTagType(tag)">
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
              <div class="product-info">
                <h3 class="product-name" :title="product.name">{{ product.name }}</h3>
                <div class="product-brand">
                  <span>品牌：{{ product.brand || '未知' }}</span>
                </div>
                <p class="product-description" :title="product.description">{{ product.description }}</p>
                <div class="product-meta">
                  <div class="product-price">
                    <span class="current-price">¥{{ product.price.toFixed(2) }}</span>
                    <span class="original-price" v-if="product.originalPrice">
                      ¥{{ product.originalPrice.toFixed(2) }}
                    </span>
                  </div>
                  <div class="product-sales">
                    <el-icon><ShoppingCart /></el-icon>
                    <span>{{ product.stock || 0 }}库存</span>
                  </div>
                </div>
                <div class="add-to-cart-action">
                  <el-button type="primary" size="small" :icon="Plus" @click.stop="addItemToCart(product)">
                    加入购物车
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="totalFilteredProducts"
            :page-sizes="[20, 40, 60]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePageSizeChange"
            @current-change="handlePageChange"
            background
          />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ShoppingCart, Plus, Picture } from '@element-plus/icons-vue';
import { useCartStore } from '@/stores/cart';
import { useProductStore } from '@/stores/product';
import { productApi } from '@/api/product';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const productStore = useProductStore();
const userStore = useUserStore();

// --- Reactive State ---
const loadingProducts = ref(true);
const activeCategory = ref('');
const priceRange = ref([0, 10000]);
const sortBy = ref('default');
const currentPage = ref(1);
const pageSize = ref(20);
const categories = ref([]);
const allProducts = ref([]);
const totalFilteredProducts = ref(0);

// --- API Functions ---
const fetchCategories = async () => {
  try {
    const data = await productStore.fetchCategories();
    console.log('获取到的分类数据:', data);
    
    // 检查每个分类的ID是否存在
    if (data && data.length > 0) {
      data.forEach((cat, index) => {
        console.log(`分类${index}:`, cat, '有ID?', !!cat.id, '有categoryId?', !!cat.categoryId);
      });
    } else {
      console.warn('没有获取到分类数据或数据为空');
    }
    
    categories.value = data;
    
    // 检查处理后的分类数据
    console.log('处理后的分类数据:', categories.value);
    
    // 延迟检查DOM中的分类元素
    nextTick(() => {
      inspectCategoryElements();
    });
  } catch (error) {
    console.error('Failed to fetch categories:', error);
    ElMessage.error('获取商品分类失败');
  }
};

// 检查DOM中的分类元素
const inspectCategoryElements = () => {
  console.log('检查DOM中的分类元素:');
  const categoryItems = document.querySelectorAll('.category-item');
  console.log(`找到 ${categoryItems.length} 个分类元素`);
  
  categoryItems.forEach((item, index) => {
    if (index > 0) { // 跳过"热门商品"
      const debugInfo = item.querySelector('.debug-info');
      console.log(`分类元素 ${index}:`, {
        text: item.textContent.trim(),
        debugInfo: debugInfo ? debugInfo.textContent : '无调试信息'
      });
    }
  });
};

const fetchProducts = async () => {
  try {
    loadingProducts.value = true;
    
    // 调试日志
    console.log('fetchProducts - 当前分类ID:', activeCategory.value, '类型:', typeof activeCategory.value);
    
    // 只有当activeCategory有值且不是空字符串时，才请求分类商品
    if (activeCategory.value && activeCategory.value.trim() !== '') {
      console.log('请求分类商品 - 分类ID:', activeCategory.value);
      
      try {
        const response = await productStore.fetchProductsByCategory(activeCategory.value, {
          pageNum: currentPage.value,
          pageSize: pageSize.value
        });
        
        console.log('分类商品响应处理:', response);
        
        // 处理响应数据
        if (response) {
          // 如果响应是数组或者有data字段
          const productData = Array.isArray(response) ? response : 
                             (response.data ? (Array.isArray(response.data) ? response.data : []) : []);
          
          allProducts.value = productData;
          totalFilteredProducts.value = productData.length;
          console.log('成功加载分类商品:', allProducts.value.length, '条记录');
        } else {
          console.warn('分类商品响应异常:', response);
          ElMessage.warning('获取分类商品失败，显示热门商品');
          // 如果获取分类商品失败，回退到热门商品
          const data = await productStore.fetchHotProducts();
          allProducts.value = data;
          totalFilteredProducts.value = data.length;
        }
      } catch (error) {
        console.error('获取分类商品出错:', error);
        ElMessage.warning('获取分类商品失败，显示热门商品');
        // 如果获取分类商品失败，回退到热门商品
        const data = await productStore.fetchHotProducts();
        allProducts.value = data;
        totalFilteredProducts.value = data.length;
      }
    } else {
      console.log('请求热门商品');
      const data = await productStore.fetchHotProducts();
      allProducts.value = data;
      totalFilteredProducts.value = data.length;
    }
  } catch (error) {
    console.error('获取商品失败:', error);
    ElMessage.error('获取商品失败');
    allProducts.value = [];
    totalFilteredProducts.value = 0;
  } finally {
    loadingProducts.value = false;
  }
};

// --- Computed Properties ---
const currentCategoryName = computed(() => {
  if (!activeCategory.value) {
    return '热门商品';
  }
  
  // 使用getCategoryId函数查找匹配的分类
  const category = categories.value.find(cat => 
    String(getCategoryId(cat)) === activeCategory.value
  );
  
  return category ? category.name : '全部商品';
});

// 排序商品
const sortProducts = (products) => {
  if (!products || products.length === 0) return [];
  
  const sorted = [...products];
  
  switch (sortBy.value) {
    case 'price-asc':
      sorted.sort((a, b) => a.price - b.price);
      break;
    case 'price-desc':
      sorted.sort((a, b) => b.price - a.price);
      break;
    case 'stock-desc':
      sorted.sort((a, b) => (b.stock || 0) - (a.stock || 0));
      break;
    default:
      // 默认排序，保持原顺序
      break;
  }
  
  return sorted;
};

// 过滤商品
const filterProducts = (products) => {
  if (!products || products.length === 0) return [];
  
  return products.filter(product => {
    // 价格过滤
    const price = product.price || 0;
    return price >= priceRange.value[0] && price <= priceRange.value[1];
  });
};

// 计算属性：分页后的商品
const paginatedProducts = computed(() => {
  // 先过滤，再排序
  const filtered = filterProducts(allProducts.value);
  const sorted = sortProducts(filtered);
  
  // 更新过滤后的总数
  totalFilteredProducts.value = sorted.length;
  
  // 计算分页
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  
  return sorted.slice(start, end);
});

// --- Methods ---
const handleCategoryClick = async (categoryId) => {
  try {
    activeCategory.value = categoryId;
    currentPage.value = 1;
    
    if (categoryId) {
      await router.push({ 
        name: 'category-with-id', 
        params: { id: categoryId }
      });
    } else {
      await router.push({ 
        name: 'category'
      });
    }
    
    await fetchProducts();
  } catch (error) {
    console.error('Failed to switch category:', error);
    ElMessage.error('切换分类失败');
  }
};

const applyFilters = () => {
  console.log('应用过滤器 - 价格范围:', priceRange.value, '排序方式:', sortBy.value);
  // 重置页码
  currentPage.value = 1;
  // 不需要重新请求数据，paginatedProducts计算属性会自动处理过滤和排序
};

const handlePageSizeChange = (size) => {
  console.log('改变每页显示数量:', size);
  pageSize.value = size;
  currentPage.value = 1;
  // 不需要重新请求数据，paginatedProducts计算属性会自动处理分页
};

const handlePageChange = (page) => {
  console.log('切换到页码:', page);
  currentPage.value = page;
  // 不需要重新请求数据，paginatedProducts计算属性会自动处理分页
};

const navigateToProduct = (productId) => {
  if (!productId) {
    console.error('无效的商品ID:', productId);
    ElMessage.error('无法查看商品详情');
    return;
  }
  console.log('导航到商品详情页:', productId);
  router.push({ name: 'product', params: { id: productId } });
};

const addItemToCart = async (product) => {
  if (!product) {
    ElMessage.error('无效的商品数据');
    return;
  }
  
  // 检查用户是否已登录
  if (!userStore.isAuthenticated) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  
  const stock = product.stock || 0;
  if (stock === 0) {
    ElMessage.warning('抱歉，该商品已售罄！');
    return;
  }
  
  try {
    const productId = product.productId;
    // 如果有多个SKU，应该让用户选择
    const skuId = product.skus && product.skus.length > 0 ? product.skus[0].skuId : null;
    await cartStore.addItem(productId, skuId, 1);
    ElMessage.success('商品已加入购物车');
  } catch (error) {
    console.error('Failed to add item to cart:', error);
    ElMessage.error('添加商品到购物车失败');
  }
};

const getTagType = (tag) => {
  if (tag === '新品') return 'success';
  if (tag === '热销') return 'danger';
  if (tag === '旗舰') return 'warning';
  return '';
};

// 获取分类的唯一ID
const getCategoryId = (category) => {
  // 尝试多种可能的ID字段
  const id = category.id || category.categoryId;
  
  // 如果找不到ID，则使用名称生成一个
  if (id !== undefined && id !== null) {
    return String(id);
  } else if (category.name) {
    // 使用分类名称生成一个ID
    console.warn(`分类没有ID字段，使用名称生成ID: ${category.name}`);
    return category.name.toLowerCase().replace(/\s+/g, '-');
  } else {
    console.error('分类既没有ID也没有名称:', category);
    return 'unknown-' + Math.random().toString(36).substring(2, 10);
  }
};

// 默认图片
const defaultImage = 'https://via.placeholder.com/300x300?text=ShopSphere';

// 处理图片加载错误
const handleImageError = (event, product) => {
  console.error(`图片加载失败: ${product.name}`, event);
  // 设置默认图片
  if (product.mainImageUrl) {
    product.mainImageUrl = defaultImage;
  } else {
    product.image = defaultImage;
  }
};

// --- Lifecycle Hooks & Watchers ---
onMounted(async () => {
  await fetchCategories();
  if (!route.params.id) {
    activeCategory.value = '';
  } else {
    activeCategory.value = route.params.id;
  }
  await fetchProducts();
});

watch(() => route.params.id, (newId) => {
  activeCategory.value = newId || '';
  currentPage.value = 1;
  fetchProducts();
});
</script>

<style scoped>
.category-container {
  max-width: 1400px;
  margin: 20px auto;
  padding: 20px;
  min-height: calc(100vh - 120px);
  background: var(--primary-gradient);
  position: relative;
  overflow: hidden;
}

.category-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;

  opacity: 0.1;
  z-index: 0;
}

.category-breadcrumb {
  margin-bottom: 25px;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.category-breadcrumb:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
}

:deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
}

:deep(.el-breadcrumb__inner) {
  color: rgba(255, 255, 255, 0.8) !important;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

:deep(.el-breadcrumb__inner:hover) {
  color: #fff !important;
  transform: translateY(-1px);
}

:deep(.el-breadcrumb__inner.is-link) {
  color: rgba(255, 255, 255, 0.6) !important;
}

:deep(.el-breadcrumb__separator) {
  color: rgba(255, 255, 255, 0.4) !important;
  margin: 0 8px;
}

@media (max-width: 768px) {
  .category-breadcrumb {
    margin-bottom: 20px;
    padding: 12px 15px;
  }
  
  :deep(.el-breadcrumb__inner) {
    font-size: 13px;
  }
}

@media (max-width: 576px) {
  .category-breadcrumb {
    margin-bottom: 15px;
    padding: 10px 12px;
  }
  
  :deep(.el-breadcrumb__inner) {
    font-size: 12px;
  }
  
  :deep(.el-breadcrumb__separator) {
    margin: 0 6px;
  }
}

.category-content {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 30px;
  position: relative;
  z-index: 1;
}

.category-sidebar {
  position: sticky;
  top: 20px;
  height: fit-content;
}

.sidebar-card {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  border-radius: 16px !important;
  margin-bottom: 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3) !important;
}

:deep(.sidebar-card .el-card__header) {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

:deep(.sidebar-card .el-card__body) {
  padding: 20px;
}

:deep(.sidebar-card h3) {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 15px;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 15px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.category-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(5px);
  color: #fff;
}

.category-item.active {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.2);
}

.category-item .el-icon {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
}

.category-item span {
  font-size: 16px;
  font-weight: 500;
}

.price-range {
  margin-top: 15px;
}

:deep(.el-slider) {
  --el-slider-main-bg-color: rgba(255, 255, 255, 0.3);
  --el-slider-runway-bg-color: rgba(255, 255, 255, 0.1);
  --el-slider-button-size: 16px;
  --el-slider-button-wrapper-size: 32px;
  --el-slider-button-hover-color: rgba(255, 255, 255, 0.4);
}

:deep(.el-slider__runway) {
  height: 4px;
}

:deep(.el-slider__bar) {
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
}

:deep(.el-slider__button) {
  border: 2px solid rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

:deep(.el-slider__button:hover) {
  transform: scale(1.2);
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}

:deep(.el-input-number) {
  --el-input-number-width: 120px;
  --el-input-bg-color: rgba(255, 255, 255, 0.1);
  --el-input-border-color: rgba(255, 255, 255, 0.2);
  --el-input-hover-border-color: rgba(255, 255, 255, 0.3);
  --el-input-focus-border-color: rgba(255, 255, 255, 0.4);
  --el-input-text-color: #fff;
  --el-input-placeholder-color: rgba(255, 255, 255, 0.6);
}

:deep(.el-input-number .el-input__wrapper) {
  box-shadow: none !important;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input-number .el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.15);
}

:deep(.el-input-number .el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.2);
}

:deep(.el-input-number .el-input-number__decrease),
:deep(.el-input-number .el-input-number__increase) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-input-number .el-input-number__decrease:hover),
:deep(.el-input-number .el-input-number__increase:hover) {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.price-separator {
  color: rgba(255, 255, 255, 0.6);
  font-size: 16px;
}

@media (max-width: 1200px) {
  .category-content {
    grid-template-columns: 1fr;
  }
  
  .category-sidebar {
    position: static;
  }
  
  .sidebar-card {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .category-content {
    gap: 20px;
  }
  
  .sidebar-card {
    margin-bottom: 15px;
  }
  
  :deep(.sidebar-card .el-card__header) {
    padding: 15px;
  }
  
  :deep(.sidebar-card .el-card__body) {
    padding: 15px;
  }
  
  :deep(.sidebar-card h3) {
    font-size: 16px;
  }
  
  .category-item {
    padding: 10px 12px;
  }
  
  .price-inputs {
    flex-direction: column;
    align-items: stretch;
  }
  
  :deep(.el-input-number) {
    --el-input-number-width: 100%;
  }
}

@media (max-width: 576px) {
  .category-content {
    gap: 15px;
  }
  
  .sidebar-card {
    margin-bottom: 10px;
  }
  
  :deep(.sidebar-card .el-card__header) {
    padding: 12px;
  }
  
  :deep(.sidebar-card .el-card__body) {
    padding: 12px;
  }
  
  .category-item {
    padding: 8px 10px;
  }
  
  .category-item .el-icon {
    font-size: 16px;
  }
  
  .category-item span {
    font-size: 13px;
  }
}

.category-main {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.toolbar-card {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  border-radius: 16px !important;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 20px;
}

.toolbar-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3) !important;
}

:deep(.toolbar-card .el-card__body) {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

:deep(.el-radio-group) {
  display: flex;
  gap: 10px;
}

:deep(.el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

:deep(.el-radio-button__inner:hover) {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  transform: translateY(-2px);
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
  box-shadow: none;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.total-products-info {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

:deep(.el-select) {
  --el-select-border-color-hover: rgba(255, 255, 255, 0.3);
  --el-select-input-focus-border-color: rgba(255, 255, 255, 0.4);
}

:deep(.el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: none !important;
  transition: all 0.3s ease;
}

:deep(.el-select .el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

:deep(.el-select .el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
}

:deep(.el-select .el-input__inner) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-select .el-input__suffix) {
  color: rgba(255, 255, 255, 0.6);
}

:deep(.el-select-dropdown) {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
}

:deep(.el-select-dropdown__item) {
  color: rgba(255, 255, 255, 0.8);
}

:deep(.el-select-dropdown__item.hover) {
  background: rgba(255, 255, 255, 0.1);
}

:deep(.el-select-dropdown__item.selected) {
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .toolbar-left {
    justify-content: center;
  }
  
  .toolbar-right {
    justify-content: center;
  }
  
  :deep(.el-radio-group) {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  :deep(.el-radio-button__inner) {
    font-size: 13px;
  }
  
  .total-products-info {
    font-size: 13px;
  }
}

@media (max-width: 576px) {
  :deep(.toolbar-card .el-card__body) {
    padding: 15px;
  }
  
  .toolbar {
    gap: 10px;
  }
  
  :deep(.el-radio-button__inner) {
    font-size: 12px;
    padding: 8px 12px;
  }
  
  .total-products-info {
    font-size: 12px;
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin: 0 !important;
}

.product-item {
  width: 100%;
}

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
  border-radius: 8px;
  overflow: hidden;
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

.product-info {
  padding: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.05);
}

.product-name {
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.3;
}

.product-brand {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 4px;
}

.product-description {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.product-meta {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.product-price {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.current-price {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.original-price {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  text-decoration: line-through;
}

.product-sales {
  display: flex;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

.add-to-cart-action {
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.add-to-cart-action .el-button {
  width: 100%;
  height: 28px;
  font-size: 12px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  transition: all 0.3s ease;
}

.add-to-cart-action .el-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.add-to-cart-action .el-button:active {
  transform: translateY(0);
}

.product-tags {
  position: absolute;
  top: 8px;
  left: 8px;
  display: flex;
  gap: 4px;
  z-index: 1;
}

:deep(.el-tag) {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  font-size: 11px;
  padding: 0 6px;
  height: 20px;
  line-height: 20px;
  border-radius: 3px;
}

:deep(.el-tag--success) {
  background: rgba(103, 194, 58, 0.2);
  border-color: rgba(103, 194, 58, 0.3);
}

:deep(.el-tag--warning) {
  background: rgba(230, 162, 60, 0.2);
  border-color: rgba(230, 162, 60, 0.3);
}

:deep(.el-tag--danger) {
  background: rgba(245, 108, 108, 0.2);
  border-color: rgba(245, 108, 108, 0.3);
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

@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
  
  .product-info {
    padding: 15px;
  }
  
  .product-name {
    font-size: 15px;
  }
  
  .product-brand {
    font-size: 13px;
  }
  
  .current-price {
    font-size: 18px;
  }
  
  .add-to-cart-action .el-button {
    height: 32px;
    font-size: 13px;
  }
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
</style>
