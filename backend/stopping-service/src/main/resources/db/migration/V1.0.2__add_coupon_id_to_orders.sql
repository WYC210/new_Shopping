-- 添加coupon_id字段到orders表
ALTER TABLE orders ADD COLUMN coupon_id BIGINT DEFAULT NULL COMMENT '优惠券ID'; 