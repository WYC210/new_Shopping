package com.wyc.domain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private Long productId;
    private String name;
    private Long categoryId;
    private String brand;
    private BigDecimal price;
    private Integer stock;
    private Boolean isOnSale;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description;
    private String specifications;
    private String mainImageUrl;
}