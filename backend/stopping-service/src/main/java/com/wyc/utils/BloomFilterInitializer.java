package com.wyc.utils;

import com.wyc.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class BloomFilterInitializer {
    @Autowired(required = false)
    private ProductMapper productMapper;


    public static BloomFilter productBloomFilter = new BloomFilter();
    public static BloomFilter categoryBloomFilter = new BloomFilter();

    @PostConstruct
    public void init() {
        // 初始化商品ID
        if (productMapper != null) {
            List<Long> productIds = productMapper.getAllProductIds();
            if (productIds != null) {
                for (Long id : productIds) {
                    productBloomFilter.add(String.valueOf(id));
                }
            }
        }
        // 初始化分类ID
        if (productMapper != null) {
            List<Long> categoryIds = productMapper.getAllCategoryIds();
            if (categoryIds != null) {
                for (Long id : categoryIds) {
                    categoryBloomFilter.add(String.valueOf(id));
                }
            }
        }
    }

    public static BloomFilter getProductBloomFilter() {
        return productBloomFilter;
    }

    public static BloomFilter getCategoryBloomFilter() {
        return categoryBloomFilter;
    }
}