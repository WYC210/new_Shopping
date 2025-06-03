package com.wyc.domain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class ProductSKUDTO {
    private Long skuId;
    private String skuCode;
    private Map<String, String> attributes;
    private BigDecimal price;
    private Integer stock;
}