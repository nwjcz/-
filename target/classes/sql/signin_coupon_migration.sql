-- 七天签到领优惠券功能 - 数据库迁移脚本
-- 数据库: db05

-- 签到记录表
CREATE TABLE IF NOT EXISTS `sign_in_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `sign_in_date` DATE NOT NULL COMMENT '签到日期',
    `consecutive_days` INT NOT NULL DEFAULT 1 COMMENT '连续签到天数(1-7)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `sign_in_date`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

-- 优惠券定义表
CREATE TABLE IF NOT EXISTS `coupon` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '优惠券金额',
    `min_order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低订单金额',
    `sign_in_day` INT NOT NULL COMMENT '关联签到天数(1/3/7)',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0-待审核, 1-已通过, 2-已拒绝, 3-已失效',
    `merchant_id` INT NOT NULL COMMENT '商家ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_sign_in_day_status` (`sign_in_day`, `status`),
    INDEX `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券定义表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS `user_coupon` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态: 0-未使用, 1-已使用, 2-已过期',
    `receive_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `use_time` DATETIME NULL COMMENT '使用时间',
    `expire_time` DATETIME NULL COMMENT '过期时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';
