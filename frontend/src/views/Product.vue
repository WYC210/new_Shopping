// 获取商品详情
const fetchProduct = async () => {
  try {
    loading.value = true;
    const response = await productApi.getProductDetail(route.params.id);
    
    if (response.code === 200) {
      product.value = response.data;
      
      // 设置默认选中的SKU
      if (product.value.skus && product.value.skus.length > 0) {
        selectedSku.value = product.value.skus[0];
      }
      
      // 记录浏览历史
      try {
        await userApi.recordBrowsing(product.value.productId, product.value.name);
      } catch (error) {
        console.error('记录浏览历史失败:', error);
      }
      
      // 获取商品评价
      await fetchReviews();
    } else {
      ElMessage.error(response.msg || '获取商品详情失败');
    }
  } catch (error) {
    console.error('获取商品详情失败:', error);
    ElMessage.error('获取商品详情失败');
  } finally {
    loading.value = false;
  }
}; 