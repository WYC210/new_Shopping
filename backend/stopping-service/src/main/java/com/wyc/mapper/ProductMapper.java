package com.wyc.mapper;

import com.wyc.domain.dto.ProductDTO;
import com.wyc.domain.dto.ProductDetailDTO;
import com.wyc.domain.dto.ProductSKUDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductMapper {
    List<ProductDTO> getHotProducts();

    List<ProductDTO> getNewProducts(@Param("days") int days);

    ProductDetailDTO getProductDetail(@Param("productId") Long productId);

    List<ProductDTO> getProductsByCategory(@Param("categoryId") Long categoryId);

    List<ProductDTO> searchProducts(@Param("keyword") String keyword);

    // 获取所有商品ID
    List<Long> getAllProductIds();

    // 获取所有分类ID
    List<Long> getAllCategoryIds();

    // 查询商品所有图片
    List<String> getProductImages(@Param("productId") Long productId);

    // 查询商品所有标签
    List<String> getProductTags(@Param("productId") Long productId);

    // 查询商品所有SKU
    List<ProductSKUDTO> getProductSKUs(@Param("productId") Long productId);
}