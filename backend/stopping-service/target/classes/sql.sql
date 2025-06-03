# -- 1. 先删除已存在的数据库（谨慎操作）
# DROP DATABASE IF EXISTS new_store;
#
# CREATE DATABASE new_store
#     CHARACTER SET utf8mb4
#     COLLATE utf8mb4_unicode_ci;
# use new_store;
-- =========================
-- 🧑 用户模块
-- =========================
use `ry-vue`;

CREATE TABLE Users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) UNIQUE COMMENT '用户邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '用户手机号',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否已注销',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_is_deleted (is_deleted)
);

CREATE TABLE addresses (
    address_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '地址ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人手机号',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    district VARCHAR(50) COMMENT '区/县',
    detail_address TEXT COMMENT '详细地址',
    postal_code VARCHAR(10) COMMENT '邮政编码',
    is_default BOOLEAN DEFAULT FALSE COMMENT '是否为默认地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
);

-- 用户优惠券表
CREATE TABLE UserCoupons (
     user_coupon_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户优惠券记录ID',
     user_id BIGINT NOT NULL COMMENT '用户ID',
     coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
     coupon_name VARCHAR(100) NOT NULL COMMENT '优惠券名称（冗余字段，方便展示）',
     status ENUM('unused', 'used', 'expired') DEFAULT 'unused' COMMENT '使用状态',
     used_at DATETIME COMMENT '使用时间',
     created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     UNIQUE (user_id, coupon_id),
     INDEX idx_user_coupon_status (user_id, status)
);

-- 用户浏览记录（登录用户）
CREATE TABLE UserBrowsingRecords (
     record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
     user_id BIGINT NOT NULL COMMENT '用户ID',
     product_id BIGINT NOT NULL COMMENT '商品ID',
     product_name VARCHAR(255) NOT NULL COMMENT '商品名称（冗余字段，方便展示浏览历史）',
     viewed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
     INDEX idx_user_product (user_id, product_id),
     INDEX idx_viewed_at (viewed_at)
);

CREATE TABLE Reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评价ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    rating TINYINT NOT NULL COMMENT '评分（1-5）',
    content TEXT COMMENT '评价内容',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_product_id (product_id),
    INDEX idx_user_rating (user_id, rating)
);

-- =========================
-- 🛒 购物模块
-- =========================

-- 购物车表
CREATE TABLE CartItems (
   cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车项ID',
   user_id BIGINT NOT NULL COMMENT '用户ID',
   product_sku_id BIGINT NOT NULL COMMENT '商品SKU ID',
   product_name VARCHAR(255) NOT NULL COMMENT '商品名称（冗余字段，用于展示）',
   quantity INT NOT NULL COMMENT '数量',
   created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
   updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
   UNIQUE (user_id, product_sku_id),
   INDEX idx_user (user_id),
   is_selected BOOLEAN DEFAULT TRUE COMMENT '是否选中'

);

-- =========================
-- 📦 订单模块
-- =========================

CREATE TABLE Orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    status ENUM('pending', 'paid', 'shipped', 'completed', 'cancelled') DEFAULT 'pending' COMMENT '订单状态',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_status (user_id, status)
);

CREATE TABLE OrderItems (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    quantity INT NOT NULL COMMENT '购买数量',
    price DECIMAL(10,2) NOT NULL COMMENT '购买时单价',
    product_name VARCHAR(255) COMMENT '下单时商品名称（冗余字段，避免关联Products）',
    product_image VARCHAR(255) COMMENT '下单时商品封面图地址（冗余字段，便于订单页展示）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order (order_id),
    INDEX idx_product (product_id)
);

-- 支付记录表
CREATE TABLE Payments (
      payment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '支付记录ID',
      order_id BIGINT NOT NULL COMMENT '订单ID',
      order_number VARCHAR(100) NOT NULL COMMENT '订单号（冗余，便于支付记录对账）',
      user_id BIGINT NOT NULL COMMENT '用户ID',
      amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
      payment_method VARCHAR(50) COMMENT '支付方式',
      paid_at DATETIME COMMENT '支付时间',
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      INDEX idx_order_user (order_id, user_id),
      INDEX idx_paid_at (paid_at)
);

-- 退款记录表
CREATE TABLE Refunds (
     refund_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '退款记录ID',
     payment_id BIGINT NOT NULL COMMENT '支付记录ID',
     order_number VARCHAR(100) NOT NULL COMMENT '订单号（冗余，便于退款记录关联订单）',
     amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
     reason TEXT COMMENT '退款原因',
     refunded_at DATETIME COMMENT '退款时间',
     created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     INDEX idx_payment_id (payment_id)
);

-- =========================
-- 🎁 优惠券模块
-- =========================

CREATE TABLE Coupons (
    coupon_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '优惠券ID',
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    description TEXT COMMENT '优惠券描述',
    discount_amount DECIMAL(10,2) NOT NULL COMMENT '优惠金额',
    min_order_amount DECIMAL(10,2) NOT NULL COMMENT '最低订单金额限制',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    total_quantity INT NOT NULL COMMENT '总发放数量',
    remaining_quantity INT NOT NULL COMMENT '剩余可领取数量',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_time (start_time, end_time),
    INDEX idx_is_active (is_active),
    INDEX idx_remaining (remaining_quantity)
);
-- =========================
-- 🛍 商品与分类模块
-- =========================

CREATE TABLE Categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类ID（支持多级分类）',
    sort_order INT DEFAULT 0 COMMENT '排序权重（越大越前）',
    is_visible BOOLEAN DEFAULT TRUE COMMENT '是否显示',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent (parent_id),
    INDEX idx_visible (is_visible)
);

CREATE TABLE Products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id BIGINT NOT NULL COMMENT '所属分类',
    brand VARCHAR(100) COMMENT '品牌名称',
    price DECIMAL(10,2) NOT NULL COMMENT '基础价格',
    stock INT DEFAULT 0 COMMENT '库存',
    is_on_sale BOOLEAN DEFAULT TRUE COMMENT '是否上架',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_sale (category_id, is_on_sale),
    INDEX idx_price (price)
);

CREATE TABLE ProductDetails (
    product_id BIGINT PRIMARY KEY COMMENT '商品ID',
    description TEXT COMMENT '商品描述',
    specifications TEXT COMMENT '规格参数JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

CREATE TABLE ProductImages (
    image_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    image_url VARCHAR(500) NOT NULL COMMENT '图片地址',
    is_main BOOLEAN DEFAULT FALSE COMMENT '是否主图',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_product_main (product_id, is_main)
);

CREATE TABLE ProductSKUs (
    sku_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'SKU ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    sku_code VARCHAR(100) NOT NULL COMMENT 'SKU编码',
    attributes JSON COMMENT '规格属性（如颜色、尺码）',
    price DECIMAL(10,2) NOT NULL COMMENT 'SKU价格',
    stock INT DEFAULT 0 COMMENT 'SKU库存',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE (product_id, sku_code),
    INDEX idx_product_sku (product_id)
);

CREATE TABLE ProductTags (
    tag_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

CREATE TABLE ProductTagRelations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    UNIQUE (product_id, tag_id),
    INDEX idx_product_tag (product_id, tag_id)
);
-- =========================
-- 🧭 游客模块（未登录用户）
-- =========================

CREATE TABLE Visitors (
    visitor_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '游客ID',
    fingerprint VARCHAR(100) NOT NULL UNIQUE COMMENT '浏览器指纹',
    first_visited_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次访问时间',
    last_visited_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后访问时间',
    user_id BIGINT DEFAULT NULL COMMENT '关联用户ID（若后续登录）',
    INDEX idx_user_id (user_id),
    INDEX idx_last_visited (last_visited_at)
);

-- 游客浏览记录
CREATE TABLE VisitorBrowsingRecords (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    visitor_id BIGINT NOT NULL COMMENT '游客ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称（冗余字段，方便展示浏览历史）',
    viewed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    INDEX idx_visitor_product (visitor_id, product_id),
    INDEX idx_viewed_at (viewed_at)
);

-- 添加优惠券类型字段
ALTER TABLE Coupons
    ADD COLUMN coupon_type ENUM('fixed', 'percentage', 'shipping') NOT NULL DEFAULT 'fixed' COMMENT '优惠券类型：固定金额/百分比折扣/免运费';

-- 添加折扣比例字段
ALTER TABLE Coupons
    ADD COLUMN discount_percentage DECIMAL(5,2) COMMENT '折扣比例(0-100)，仅对百分比折扣券有效';

-- 添加适用范围字段
ALTER TABLE Coupons
    ADD COLUMN applicable_scope ENUM('all', 'category') DEFAULT 'all' COMMENT '适用范围：全部/特定分类/特定商品';

-- 给标签添加索引
ALTER TABLE ProductTags ADD INDEX idx_name (name);

use `new_store`;
-- 给用户添加金额
ALTER TABLE Users
    ADD COLUMN balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '用户账户余额（单位：元）',
    ADD COLUMN balance_updated_at DATETIME COMMENT '余额最后变动时间';
use `new_store`;
ALTER TABLE Users
    ADD COLUMN Uuid VARCHAR(256) NOT NULL  COMMENT '设置用户Uuid';
