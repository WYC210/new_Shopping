package com.wyc.mapper;

import com.wyc.domain.dto.ProductDTO;
import com.wyc.domain.dto.ProductDetailDTO;
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
}