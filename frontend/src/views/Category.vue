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
              <el-icon><Star /></el-icon>
              <span>热门商品</span>
            </div>
            <div 
              v-for="(category, index) in categories" 
              :key="index"
              class="category-item"
              :class="{ active: activeCategory === String(getCategoryId(category)) }"
              @click="handleCategoryClick(getCategoryId(category))"
            >
              <el-icon>
                <component :is="category.icon" />
              </el-icon>
              <span>{{ category.name }}</span>
              <span class="debug-info">(ID: {{ getCategoryId(category) }})</span>
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

        <el-row :gutter="20" class="product-grid" v-loading="loadingProducts">
          <el-empty v-if="paginatedProducts.length === 0 && !loadingProducts" description="暂无商品符合条件"></el-empty>
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in paginatedProducts" :key="product.productId || product.id">
            <el-card shadow="hover" class="product-card" @click="navigateToProduct(product.productId || product.id)">
              <div class="product-image-wrapper">
                <el-image :src="product.mainImageUrl || product.image" fit="cover" loading="lazy" />
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
          </el-col>
        </el-row>

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
import { ref, onMounted, computed, watch, markRaw, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { HomeFilled, ShoppingBag, Present, Food, Reading, ShoppingCart, Plus, Star } from '@element-plus/icons-vue';
import { useCartStore } from '@/stores/cart';
import { useProductStore } from '@/stores/product';
import { productApi } from '@/api/product';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const productStore = useProductStore();

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
    
    categories.value = data.map(category => ({
      ...category,
      icon: getCategoryIcon(category.name)
    }));
    
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

// 根据分类名称返回对应的图标
const getCategoryIcon = (categoryName) => {
  const iconMap = {
    '手机数码': markRaw(HomeFilled),
    '家用电器': markRaw(HomeFilled),
    '服装鞋包': markRaw(ShoppingBag),
    '美妆个护': markRaw(Present),
    '食品生鲜': markRaw(Food),
    '图书文具': markRaw(Reading)
  };
  return iconMap[categoryName] || markRaw(HomeFilled);
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
  
  const stock = product.stock || 0;
  if (stock === 0) {
    ElMessage.warning('抱歉，该商品已售罄！');
    return;
  }
  
  try {
    await cartStore.addItem({
      productId: product.productId || product.id,
      name: product.name,
      price: product.price,
      imageUrl: product.mainImageUrl || product.image,
      quantity: 1,
      stock: stock
    });
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
  background-color: #f0f2f5;
  border-radius: 12px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  min-height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.category-breadcrumb {
  margin-bottom: 25px;
  padding: 10px 0;
  font-size: 15px;
  color: #606266;
}

.category-content {
  display: flex;
  gap: 30px;
}

.category-sidebar {
  width: 280px;
  flex-shrink: 0;
}

.sidebar-card {
  margin-bottom: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
  border: none;
}

.sidebar-card h3 {
  margin: 0 0 20px;
  font-size: 20px;
  color: #333;
  font-weight: 600;
  border-bottom: 1px solid #e6e6e6;
  padding-bottom: 15px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #606266;
}

.category-item:hover {
  background-color: #f5f7fa;
  color: #409EFF;
}

.category-item.active {
  background-color: #ecf5ff;
  color: #409EFF;
  font-weight: 500;
}

.category-item .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.category-item span {
  font-size: 14px;
}

.price-range {
  padding: 0 10px;
  margin-bottom: 10px;
}

.price-inputs {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}

.price-separator {
  color: #909399;
  font-size: 18px;
  margin: 0 8px;
}

.category-main {
  flex: 1;
}

.toolbar-card {
  margin-bottom: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
  padding: 15px 25px;
  border: none;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.toolbar-left .el-radio-group {
  border-radius: 8px;
  overflow: hidden;
}

.toolbar-left .el-radio-button {
  --el-radio-button-checked-bg-color: #2196F3;
  --el-radio-button-checked-text-color: #fff;
  --el-radio-button-checked-border-color: #2196F3;
  --el-radio-button-border-color: #d9e0e7;
  --el-radio-button-text-color: #606266;
}

.toolbar-left .el-radio-button__inner {
  font-size: 15px;
  padding: 10px 20px;
  border-radius: 0;
}

.toolbar-left .el-radio-button:first-child .el-radio-button__inner {
  border-top-left-radius: 8px;
  border-bottom-left-radius: 8px;
}

.toolbar-left .el-radio-button:last-child .el-radio-button__inner {
  border-top-right-radius: 8px;
  border-bottom-right-radius: 8px;
}

.total-products-info {
  font-size: 15px;
  color: #777;
  margin-right: 20px;
}

.el-select {
  width: 120px;
}

.product-grid {
  margin-left: -15px !important;
  margin-right: -15px !important;
}

.el-col {
  padding-left: 15px !important;
  padding-right: 15px !important;
  margin-bottom: 30px;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  border: none;
}

.product-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
}

.product-image-wrapper {
  position: relative;
  height: 220px;
  overflow: hidden;
  border-bottom: 1px solid #f0f0f0;
}

.product-image-wrapper .el-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.4s ease;
}

.product-card:hover .product-image-wrapper .el-image {
  transform: scale(1.08);
}

.product-tags {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  gap: 6px;
  z-index: 2;
}

.product-tags .el-tag {
  font-size: 12px;
  padding: 0 8px;
  height: 24px;
  line-height: 24px;
  border-radius: 4px;
}

.product-info {
  padding: 18px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  margin: 0 0 10px;
  font-size: 18px;
  color: #333;
  font-weight: 600;
  height: 48px;
  line-height: 24px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-brand {
  margin: 5px 0 10px;
}

.product-brand span {
  font-size: 13px;
  color: #666;
  background-color: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
}

.product-description {
  margin: 0 0 15px;
  font-size: 14px;
  color: #888;
  height: 40px;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: auto;
  margin-bottom: 15px;
}

.product-price {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.current-price {
  color: #e63946;
  font-size: 22px;
  font-weight: bold;
}

.original-price {
  color: #b0b0b0;
  font-size: 14px;
  text-decoration: line-through;
  margin-top: 2px;
}

.product-sales {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #888;
  font-size: 14px;
}

.product-sales .el-icon {
  font-size: 16px;
  color: #b0b0b0;
}

.add-to-cart-action {
  text-align: center;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.add-to-cart-action .el-button {
  width: 90%;
  font-size: 15px;
  padding: 10px 0;
  border-radius: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 25px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
  margin-top: 30px;
}

@media (max-width: 992px) {
  .category-content {
    flex-direction: column;
    gap: 20px;
  }
  .category-sidebar {
    width: 100%;
    margin-bottom: 0;
  }
  .product-grid {
    margin-left: -10px !important;
    margin-right: -10px !important;
  }
  .el-col {
    padding-left: 10px !important;
    padding-right: 10px !important;
  }
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .toolbar-right {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

@media (max-width: 768px) {
  .category-container {
    padding: 15px;
  }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .sidebar-card h3 {
    font-size: 18px;
  }
  .category-item span {
    font-size: 15px;
  }
  .product-image-wrapper {
    height: 180px;
  }
  .product-info {
    padding: 15px;
  }
  .product-name {
    font-size: 16px;
    height: auto;
  }
  .current-price {
    font-size: 20px;
  }
  .original-price, .product-sales span {
    font-size: 13px;
  }
}

@media (max-width: 576px) {
  .category-container {
    padding: 10px;
  }
  .product-grid {
    grid-template-columns: 1fr;
  }
  .sidebar-card {
    padding: 15px;
  }
  .sidebar-card h3 {
    font-size: 16px;
  }
  .toolbar-left .el-radio-button__inner {
    font-size: 13px;
    padding: 8px 12px;
  }
  .total-products-info {
    font-size: 13px;
  }
  .el-select {
    width: 100px;
  }
  .product-image-wrapper {
    height: 160px;
  }
  .product-info {
    padding: 12px;
  }
  .product-name {
    font-size: 15px;
  }
  .product-description {
    font-size: 12px;
  }
  .current-price {
    font-size: 18px;
  }
  .add-to-cart-action .el-button {
    font-size: 13px;
    padding: 8px 0;
  }
}
</style>
