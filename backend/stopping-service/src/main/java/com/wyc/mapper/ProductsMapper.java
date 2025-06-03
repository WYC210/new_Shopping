package com.wyc.mapper;

import com.wyc.domain.po.Products;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductsMapper {
    /**
     * 根据ID查询商品
     */
    Products selectById(@Param("productId") Long productId);

    /**
     * 更新商品库存
     * 
     * @param productId 商品ID
     * @param quantity  库存变化量（正数增加，负数减少）
     * @return 影响行数
     */
    int updateStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}