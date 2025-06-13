package com.wyc.domain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDetailDTO {
    private Long productId;
    private String name;
    private Long categoryId;
    private String brand;
    private BigDecimal price;
    private Integer stock;
    private Boolean isOnSale;
    private String description;
    private String specifications;
    private List<String> imageUrls;
    private List<ProductSKUDTO> skus;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}