-- 添加商品ID字段到购物车表
ALTER TABLE CartItems ADD COLUMN product_id BIGINT AFTER user_id COMMENT '商品ID';
 
-- 更新现有记录，将product_sku_id的值复制到product_id字段
-- 这是临时的解决方案，后续需要根据SKU查询到真正的商品ID并更新
UPDATE CartItems SET product_id = (
    SELECT product_id FROM ProductSKUs WHERE sku_id = product_sku_id
) WHERE 1=1; 