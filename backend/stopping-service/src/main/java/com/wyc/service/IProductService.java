package com.wyc.service;

import com.wyc.domain.dto.ProductDTO;
import com.wyc.domain.dto.ProductDetailDTO;
import java.util.List;

public interface IProductService {
    List<ProductDTO> getHotProducts();

    List<ProductDTO> getNewProducts();

    ProductDetailDTO getProductDetail(Long productId);

    List<ProductDTO> getProductsByCategory(Long categoryId);

    List<ProductDTO> searchProducts(String keyword);
}