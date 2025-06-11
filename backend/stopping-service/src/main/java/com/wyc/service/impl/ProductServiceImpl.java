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
import com.wyc.utils.RedisCache;
import com.wyc.utils.BloomFilter;
import org.springframework.beans.factory.annotation.Value;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.wyc.utils.BloomFilterInitializer;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductServiceImpl implements IProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisCache redisCache;

    private static final String HOT_PRODUCTS_KEY = "hot_products";
    private static final String NEW_PRODUCTS_KEY = "new_products";
    private static final String PRODUCT_DETAIL_KEY_PREFIX = "product_detail_";
    private static final String CATEGORY_PRODUCTS_KEY_PREFIX = "category_products_";
    private static final int CACHE_BASE_TIMEOUT = 60 * 10; // 10分钟
    private static final int CACHE_RANDOM_BOUND = 60 * 5; // 随机5分钟
    private static final String PRODUCT_BLOOM_FILTER_KEY = "bloom_product";
    private static final String CATEGORY_BLOOM_FILTER_KEY = "bloom_category";
    private static final ProductDTO EMPTY_PRODUCT_DTO = new ProductDTO();
    private static final ProductDetailDTO EMPTY_PRODUCT_DETAIL_DTO = new ProductDetailDTO();
    private static final List<ProductDTO> EMPTY_PRODUCT_LIST = Collections.emptyList();

    private static final BloomFilter productBloomFilter = new BloomFilter();
    private static final BloomFilter categoryBloomFilter = new BloomFilter();
    private static final Random random = new Random();
    private static final ConcurrentHashMap<String, Object> localLocks = new ConcurrentHashMap<>();

    @Override
    public List<ProductDTO> getHotProducts() {
        String cacheKey = HOT_PRODUCTS_KEY;
        List<ProductDTO> cached = redisCache.getCacheList(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        List<ProductDTO> products = productMapper.getHotProducts();
        if (products == null || products.isEmpty()) {
            redisCache.setCacheList(cacheKey, EMPTY_PRODUCT_LIST);
            redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
            return EMPTY_PRODUCT_LIST;
        }
        redisCache.setCacheList(cacheKey, products);
        redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
        return products;
    }

    @Override
    public List<ProductDTO> getNewProducts() {
        String cacheKey = NEW_PRODUCTS_KEY;
        List<ProductDTO> cached = redisCache.getCacheList(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        List<ProductDTO> products = productMapper.getNewProducts(7);
        if (products == null || products.isEmpty()) {
            redisCache.setCacheList(cacheKey, EMPTY_PRODUCT_LIST);
            redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
            return EMPTY_PRODUCT_LIST;
        }
        redisCache.setCacheList(cacheKey, products);
        redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
        return products;
    }

    @Override
    public ProductDetailDTO getProductDetail(Long productId) {
        // 布隆过滤器防击穿
        if (!BloomFilterInitializer.getProductBloomFilter().mightContain(productId.toString())) {
            return EMPTY_PRODUCT_DETAIL_DTO;
        }
        String cacheKey = PRODUCT_DETAIL_KEY_PREFIX + productId;
        ProductDetailDTO cached = redisCache.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }
        // 本地锁防击穿
        Object lock = localLocks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            cached = redisCache.getCacheObject(cacheKey);
            if (cached != null) {
                return cached;
            }
            ProductDetailDTO detail = productMapper.getProductDetail(productId);
            if (detail == null) {
                redisCache.setCacheObject(cacheKey, EMPTY_PRODUCT_DETAIL_DTO,
                        CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
                return EMPTY_PRODUCT_DETAIL_DTO;
            }
            // 兜底处理图片
            if (detail.getImageUrls() == null || detail.getImageUrls().isEmpty()) {
                List<String> defaultImages = new ArrayList<>();
                defaultImages.add("https://img14.360buyimg.com/n1/jfs/t1/123456/40/12345/1234567890.jpg"); // 你可以替换为自己的默认图片
                detail.setImageUrls(defaultImages);
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

            redisCache.setCacheObject(cacheKey, detail, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND),
                    TimeUnit.SECONDS);
            return detail;
        }
    }

    @Override
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        if (!BloomFilterInitializer.getCategoryBloomFilter().mightContain(categoryId.toString())) {
            return EMPTY_PRODUCT_LIST;
        }
        String cacheKey = CATEGORY_PRODUCTS_KEY_PREFIX + categoryId;
        List<ProductDTO> cached = redisCache.getCacheList(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        Object lock = localLocks.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            cached = redisCache.getCacheList(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }
            List<ProductDTO> products = productMapper.getProductsByCategory(categoryId);
            if (products == null || products.isEmpty()) {
                redisCache.setCacheList(cacheKey, EMPTY_PRODUCT_LIST);
                redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
                return EMPTY_PRODUCT_LIST;
            }
            redisCache.setCacheList(cacheKey, products);
            redisCache.expire(cacheKey, CACHE_BASE_TIMEOUT + random.nextInt(CACHE_RANDOM_BOUND), TimeUnit.SECONDS);
            return products;
        }
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