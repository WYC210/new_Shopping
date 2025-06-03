package com.wyc.mapper;

import com.wyc.domain.po.ProductSkus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductSkusMapper {
    int insert(ProductSkus sku);

    int updateById(ProductSkus sku);

    int deleteById(@Param("skuId") Long skuId);

    ProductSkus selectById(@Param("skuId") Long skuId);

    List<ProductSkus> selectByProductId(@Param("productId") Long productId);

    ProductSkus selectByProductIdAndSkuCode(@Param("productId") Long productId, @Param("skuCode") String skuCode);
}