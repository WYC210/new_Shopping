-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: localhost      Database: new_store
-- ------------------------------------------------------
-- Server version 8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

---
-- Table structure for table `cartitems`
---

DROP TABLE IF EXISTS `cartitems`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cartitems`
(
    `cart_item_id`   bigint                                  NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    `user_id`        bigint                                  NOT NULL COMMENT '用户ID',
    `product_sku_id` bigint                                  NOT NULL COMMENT '商品SKU ID',
    `product_name`   varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称（冗余字段，用于展示）',
    `quantity`       int                                     NOT NULL COMMENT '数量',
    `created_at`     datetime   DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `updated_at`     datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_selected`    tinyint(1) DEFAULT '1' COMMENT '是否选中',
    `is_deleted`     tinyint(1) DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`cart_item_id`),
    UNIQUE KEY `user_id` (`user_id`, `product_sku_id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 10
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `categories`
---

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories`
(
    `category_id` bigint                                  NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
    `parent_id`   bigint     DEFAULT NULL COMMENT '父分类ID（支持多级分类）',
    `sort_order`  int        DEFAULT '0' COMMENT '排序权重（越大越前）',
    `is_visible`  tinyint(1) DEFAULT '1' COMMENT '是否显示',
    `created_at`  datetime   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`category_id`),
    KEY `idx_parent` (`parent_id`),
    KEY `idx_visible` (`is_visible`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 9
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `coupons`
---

DROP TABLE IF EXISTS `coupons`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupons`
(
    `coupon_id`           bigint                                                            NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
    `name`                varchar(100) COLLATE utf8mb4_unicode_ci                           NOT NULL COMMENT '优惠券名称',
    `description`         text COLLATE utf8mb4_unicode_ci COMMENT '优惠券描述',
    `discount_amount`     decimal(10, 2)                                                    NOT NULL COMMENT '优惠金额',
    `min_order_amount`    decimal(10, 2)                                                    NOT NULL COMMENT '最低订单金额限制',
    `start_time`          datetime                                                          NOT NULL COMMENT '开始时间',
    `end_time`            datetime                                                          NOT NULL COMMENT '结束时间',
    `total_quantity`      int                                                               NOT NULL COMMENT '总发放数',
    `remaining_quantity`  int                                                               NOT NULL COMMENT '剩余可领取数量',
    `per_limit`           int                                                               NOT NULL DEFAULT 1 COMMENT '每人限领数量',
    `is_active`           tinyint(1)                                                                 DEFAULT '1' COMMENT '是否启用',
    `created_at`          datetime                                                                   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          datetime                                                                   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `coupon_type`         enum ('fixed','percentage','shipping') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'fixed' COMMENT '优惠券类型：固定金额/百分比折扣/免运费',
    `discount_percentage` decimal(5, 2)                                                              DEFAULT NULL COMMENT '折扣比例(0-100)，仅对百分比折扣券有效',
    `applicable_scope`    enum ('all','category') COLLATE utf8mb4_unicode_ci                         DEFAULT 'all' COMMENT '适用范围：全部/特定分类/特定商品',
    PRIMARY KEY (`coupon_id`),
    KEY `idx_time` (`start_time`, `end_time`),
    KEY `idx_is_active` (`is_active`),
    KEY `idx_remaining` (`remaining_quantity`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `orderitems`
---

DROP TABLE IF EXISTS `orderitems`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orderitems`
(
    `order_item_id`  bigint         NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
    `order_id`       bigint         NOT NULL COMMENT '订单ID',
    `product_id`     bigint         NOT NULL COMMENT '商品ID',
    `product_sku_id` bigint         NOT NULL COMMENT 'SKU ID',
    `quantity`       int            NOT NULL COMMENT '购买数量',
    `price`          decimal(10, 2) NOT NULL COMMENT '购买时单价',
    `product_name`   varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '下单时商品名称（冗余字段，避免关联Products）',
    `product_image`  varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '下单时商品封面图地址（冗余字段，便于订单页展示）',
    `created_at`     datetime                                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`order_item_id`),
    KEY `idx_order` (`order_id`),
    KEY `idx_product` (`product_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 10
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `orders`
---

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders`
(
    `order_id`     bigint         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `user_id`      bigint         NOT NULL COMMENT '用户ID',
    `status`       enum ('pending','paid','shipped','completed','cancelled') COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT '订单状态',
    `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
    `coupon_id` BIGINT DEFAULT NULL COMMENT '优惠券ID',
`created_at`   datetime   DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `updated_at`   datetime   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`order_id`),
    KEY `idx_user_status` (`user_id`, `status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 12
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `payments`
---

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments`
(
    `payment_id`     bigint                                  NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
    `order_id`       bigint                                  NOT NULL COMMENT '订单ID',
    `order_number`   varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号（冗余，便于支付记录对账）',
    `user_id`        bigint                                  NOT NULL COMMENT '用户ID',
    `amount`         decimal(10, 2)                          NOT NULL COMMENT '支付金额',
    `payment_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付方式',
    `paid_at`        datetime                               DEFAULT NULL COMMENT '支付时间',
    `created_at`     datetime                               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`payment_id`),
    KEY `idx_order_user` (`order_id`, `user_id`),
    KEY `idx_paid_at` (`paid_at`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `productdetails`
---

DROP TABLE IF EXISTS `productdetails`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productdetails`
(
    `product_id`     bigint NOT NULL COMMENT '商品ID',
    `description`    text COLLATE utf8mb4_unicode_ci COMMENT '商品描述',
    `specifications` text COLLATE utf8mb4_unicode_ci COMMENT '规格参数JSON',
    `created_at`     datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`product_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `productimages`
---

DROP TABLE IF EXISTS `productimages`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productimages`
(
    `image_id`   bigint                                  NOT NULL AUTO_INCREMENT COMMENT '图片ID',
    `product_id` bigint                                  NOT NULL COMMENT '商品ID',
    `image_url`  varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片地址',
    `is_main`    tinyint(1) DEFAULT '0' COMMENT '是否主图',
    `sort_order` int        DEFAULT '0' COMMENT '排序',
    `created_at` datetime   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`image_id`),
    KEY `idx_product_main` (`product_id`, `is_main`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 101
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `products`
---

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products`
(
    `product_id`  bigint                                  NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name`        varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
    `category_id` bigint                                  NOT NULL COMMENT '所属分类',
    `brand`       varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌名称',
    `price`       decimal(10, 2)                          NOT NULL COMMENT '基础价格',
    `stock`       int                                     DEFAULT '0' COMMENT '库存',
    `is_on_sale`  tinyint(1)                              DEFAULT '1' COMMENT '是否上架',
    `created_at`  datetime                                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  datetime                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`product_id`),
    KEY `idx_category_sale` (`category_id`, `is_on_sale`),
    KEY `idx_price` (`price`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 51
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `productskus`
---

DROP TABLE IF EXISTS `productskus`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productskus`
(
    `sku_id`     bigint                                  NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
    `product_id` bigint                                  NOT NULL COMMENT '商品ID',
    `sku_code`   varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
    `attributes` json     DEFAULT NULL COMMENT '规格属性（如颜色、尺码）',
    `price`      decimal(10, 2)                          NOT NULL COMMENT 'SKU价格',
    `stock`      int      DEFAULT '0' COMMENT 'SKU库存',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`sku_id`),
    UNIQUE KEY `product_id` (`product_id`, `sku_code`),
    KEY `idx_product_sku` (`product_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 51
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `producttagrelations`
---

DROP TABLE IF EXISTS `producttagrelations`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producttagrelations`
(
    `id`         bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `product_id` bigint NOT NULL COMMENT '商品ID',
    `tag_id`     bigint NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `product_id` (`product_id`, `tag_id`),
    KEY `idx_product_tag` (`product_id`, `tag_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 78
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `producttags`
---

DROP TABLE IF EXISTS `producttags`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producttags`
(
    `tag_id`     bigint                                 NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name`       varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`tag_id`),
    UNIQUE KEY `name` (`name`),
    KEY `idx_name` (`name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `refunds`
---

DROP TABLE IF EXISTS `refunds`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refunds`
(
    `refund_id`    bigint                                  NOT NULL AUTO_INCREMENT COMMENT '退款记录ID',
    `payment_id`   bigint                                  NOT NULL COMMENT '支付记录ID',
    `order_number` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号（冗余，便于退款记录关联订单）',
    `amount`       decimal(10, 2)                          NOT NULL COMMENT '退款金额',
    `reason`       text COLLATE utf8mb4_unicode_ci COMMENT '退款原因',
    `refunded_at`  datetime DEFAULT NULL COMMENT '退款时间',
    `created_at`   datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`refund_id`),
    KEY `idx_payment_id` (`payment_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `reviews`
---

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews`
(
    `review_id`  bigint  NOT NULL AUTO_INCREMENT COMMENT '评价ID',
    `user_id`    bigint  NOT NULL COMMENT '用户ID',
    `product_id` bigint  NOT NULL COMMENT '商品ID',
    `rating`     tinyint NOT NULL COMMENT '评分',
    `content`    text COLLATE utf8mb4_unicode_ci COMMENT '评价内容',
    `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否已删除',
    `created_at` datetime   DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`review_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_user_rating` (`user_id`, `rating`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `useraddresses`
---

DROP TABLE IF EXISTS `useraddresses`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `useraddresses`
(
    `address_id`     bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `user_id`        bigint NOT NULL COMMENT '用户ID',
    `province`       varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份',
    `city`           varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
    `district`       varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区',
    `detail_address` text COLLATE utf8mb4_unicode_ci COMMENT '详细地址',
    `is_default`     tinyint(1)                             DEFAULT '0' COMMENT '是否为默认地址',
    `is_deleted`     tinyint(1)                             DEFAULT '0' COMMENT '是否已删除',
    `created_at`     datetime                               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     datetime                               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `receiver_name`  varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货人姓名',
    `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货人电话',
    PRIMARY KEY (`address_id`),
    KEY `idx_user_deleted` (`user_id`, `is_deleted`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `userBrowserecords`
---

DROP TABLE IF EXISTS `userBrowserecords`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `userBrowserecords`
(
    `record_id`    bigint                                  NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`      bigint                                  NOT NULL COMMENT '用户ID',
    `product_id`   bigint                                  NOT NULL COMMENT '商品ID',
    `product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
    `viewed_at`    datetime DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    PRIMARY KEY (`record_id`),
    KEY `idx_user_product` (`user_id`, `product_id`),
    KEY `idx_viewed_at` (`viewed_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `usercoupons`
---

DROP TABLE IF EXISTS `usercoupons`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usercoupons`
(
    `user_coupon_id` bigint                                  NOT NULL AUTO_INCREMENT COMMENT '用户优惠券记录ID',
    `user_id`        bigint                                  NOT NULL COMMENT '用户ID',
    `coupon_id`      bigint                                  NOT NULL COMMENT '优惠券ID',
    `coupon_name`    varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券名称（冗余字段，方便展示）',
    `status`         enum ('unused','used','expired') COLLATE utf8mb4_unicode_ci DEFAULT 'unused' COMMENT '使用状态',
    `used_at`        datetime                                                    DEFAULT NULL COMMENT '使用时间',
    `created_at`     datetime                                                    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`user_coupon_id`),
    UNIQUE KEY `user_id` (`user_id`, `coupon_id`),
    KEY `idx_user_coupon_status` (`user_id`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `users`
---

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users`
(
    `user_id`            bigint                                  NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`           varchar(50) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '用户名',
    `email`              varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户邮箱',
    `phone`              varchar(20) COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '用户手机号',
    `password_hash`      varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希值',
    `is_deleted`         tinyint(1)                              DEFAULT '0' COMMENT '是否已注销',
    `created_at`         datetime                                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         datetime                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `balance`            decimal(12, 2)                          DEFAULT '0.00' COMMENT '用户账户余额（单位：元）',
    `balance_updated_at` datetime                                DEFAULT NULL COMMENT '余额最后变动时间',
    `Uuid`               varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设置用户Uuid',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `username` (`username`),
    UNIQUE KEY `email` (`email`),
    UNIQUE KEY `phone` (`phone`),
    KEY `idx_email` (`email`),
    KEY `idx_phone` (`phone`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 17
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `visitorBrowserecords`
---

DROP TABLE IF EXISTS `visitorBrowserecords`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visitorBrowserecords`
(
    `record_id`    bigint                                  NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `visitor_id`   bigint                                  NOT NULL COMMENT '游客ID',
    `product_id`   bigint                                  NOT NULL COMMENT '商品ID',
    `product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称（冗余字段，方便展示浏览历史）',
    `viewed_at`    datetime DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
    PRIMARY KEY (`record_id`),
    KEY `idx_visitor_product` (`visitor_id`, `product_id`),
    KEY `idx_viewed_at` (`viewed_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

---
-- Table structure for table `visitors`
---

DROP TABLE IF EXISTS `visitors`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `visitors`
(
    `visitor_id`       bigint                                  NOT NULL AUTO_INCREMENT COMMENT '游客ID',
    `fingerprint`      varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '浏览器指纹',
    `first_visited_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '首次访问时间',
    `last_visited_at`  datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后访问时间',
    `user_id`          bigint   DEFAULT NULL COMMENT '关联用户ID（若后续登录）',
    PRIMARY KEY (`visitor_id`),
    UNIQUE KEY `fingerprint` (`fingerprint`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_last_visited` (`last_visited_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2025-06-04 20:46:03


-- 创建消息处理记录表
CREATE TABLE IF NOT EXISTS `message_process_record`
(
    `id`           bigint(20)  NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `message_id`   varchar(64) NOT NULL COMMENT '消息ID',
    `message_type` varchar(32) NOT NULL COMMENT '消息类型',
    `status`       tinyint(4)  NOT NULL DEFAULT '0' COMMENT '处理状态：0-处理中，1-处理成功，2-处理失败',
    `remark`       varchar(255)         DEFAULT NULL COMMENT '备注',
    `created_at`   datetime    NOT NULL COMMENT '创建时间',
    `updated_at`   datetime    NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_id` (`message_id`),
    KEY `idx_message_type` (`message_type`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息处理记录表';
