package com.wyc.service.impl;

import com.wyc.domain.dto.ProductDTO;
import com.wyc.domain.dto.ProductDetailDTO;
import com.wyc.mapper.ProductMapper;
import com.wyc.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<ProductDTO> getHotProducts() {
        // 获取热门商品列表
        return productMapper.getHotProducts();
    }

    @Override
    public List<ProductDTO> getNewProducts() {
        // 获取最近7天的新上架商品
        return productMapper.getNewProducts(7);
    }

    @Override
    public ProductDetailDTO getProductDetail(Long productId) {
        // 获取商品详情，包括SKU信息
        ProductDetailDTO detail = productMapper.getProductDetail(productId);
        if (detail == null) {
            throw new RuntimeException("商品不存在");
        }

        logger.info("获取到商品详情: productId={}", productId);

        // 处理imageUrls字段
        Object imageUrlsObj = detail.getImageUrls();
        logger.info("原始imageUrls类型: {}, 值: {}",
                imageUrlsObj != null ? imageUrlsObj.getClass().getName() : "null",
                imageUrlsObj);

        if (imageUrlsObj instanceof String) {
            String imageUrlsStr = (String) imageUrlsObj;
            if (imageUrlsStr != null && !imageUrlsStr.isEmpty()) {
                // 如果字符串中包含逗号，说明有多个URL，需要分割
                if (imageUrlsStr.contains(",")) {
                    detail.setImageUrls(Arrays.asList(imageUrlsStr.split(",")));
                } else {
                    // 只有一个URL，直接创建单元素列表
                    List<String> singleImageList = new ArrayList<>();
                    singleImageList.add(imageUrlsStr);
                    detail.setImageUrls(singleImageList);
                }
                logger.info("处理后的imageUrls: {}", detail.getImageUrls());
            } else {
                detail.setImageUrls(Collections.emptyList());
            }
        } else if (imageUrlsObj == null) {
            detail.setImageUrls(Collections.emptyList());
            logger.info("没有找到图片，设置为空列表");
        }

        // 处理tags字段
        Object tagsObj = detail.getTags();
        logger.info("原始tags类型: {}, 值: {}",
                tagsObj != null ? tagsObj.getClass().getName() : "null",
                tagsObj);

        if (tagsObj instanceof String) {
            String tagsStr = (String) tagsObj;
            if (tagsStr != null && !tagsStr.isEmpty()) {
                // 如果字符串中包含逗号，说明有多个标签，需要分割
                if (tagsStr.contains(",")) {
                    detail.setTags(Arrays.asList(tagsStr.split(",")));
                } else {
                    // 只有一个标签，直接创建单元素列表
                    List<String> singleTagList = new ArrayList<>();
                    singleTagList.add(tagsStr);
                    detail.setTags(singleTagList);
                }
                logger.info("处理后的tags: {}", detail.getTags());
            } else {
                detail.setTags(Collections.emptyList());
            }
        } else if (tagsObj == null) {
            detail.setTags(Collections.emptyList());
            logger.info("没有找到标签，设置为空列表");
        }

        return detail;
    }

    @Override
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        // 获取指定分类下的所有商品
        return productMapper.getProductsByCategory(categoryId);
    }

    @Override
    public List<ProductDTO> searchProducts(String keyword) {
        // 搜索商品，支持商品名称、品牌等字段的模糊搜索
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        return productMapper.searchProducts(keyword.trim());
    }
}