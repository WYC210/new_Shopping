-- 创建消息处理记录表
CREATE TABLE IF NOT EXISTS `message_process_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `message_id` varchar(64) NOT NULL COMMENT '消息ID',
  `message_type` varchar(32) NOT NULL COMMENT '消息类型',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '处理状态：0-处理中，1-处理成功，2-处理失败',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `updated_at` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_id` (`message_id`),
  KEY `idx_message_type` (`message_type`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息处理记录表'; 