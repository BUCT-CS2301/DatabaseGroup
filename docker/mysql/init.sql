CREATE TABLE IF NOT EXISTS `sys_sensitive_word` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `word` VARCHAR(100) NOT NULL COMMENT '敏感词',
    `category` VARCHAR(50) DEFAULT '' COMMENT '分类',
    `level` INT DEFAULT 1 COMMENT '级别(1-5)',
    `replace_with` VARCHAR(100) DEFAULT '' COMMENT '替换内容',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_word` (`word`),
    INDEX `idx_category` (`category`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

CREATE TABLE IF NOT EXISTS `audit_content` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `content` TEXT NOT NULL COMMENT '审核内容',
    `content_type` VARCHAR(50) DEFAULT '' COMMENT '内容类型',
    `source_id` VARCHAR(100) DEFAULT '' COMMENT '来源ID',
    `status` INT DEFAULT 1 COMMENT '状态(1待审核,2检测到敏感词,3通过,4拒绝)',
    `sensitive_words` VARCHAR(1000) DEFAULT '' COMMENT '检测到的敏感词',
    `audit_result` VARCHAR(20) DEFAULT '' COMMENT '审核结果(PASS/REJECT)',
    `auditor` VARCHAR(100) DEFAULT '' COMMENT '审核人',
    `audit_time` DATETIME NULL COMMENT '审核时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '审核备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_status` (`status`),
    INDEX `idx_content_type` (`content_type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容审核表';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    `description` VARCHAR(500) DEFAULT '' COMMENT '角色描述',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `permissions` TEXT COMMENT '权限列表(逗号分隔)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_code` (`code`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(100) DEFAULT '' COMMENT '昵称',
    `email` VARCHAR(200) DEFAULT '' COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
    `role_id` BIGINT DEFAULT 0 COMMENT '角色ID',
    `status` INT DEFAULT 1 COMMENT '状态(0禁用,1启用)',
    `last_login_time` DATETIME NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT '' COMMENT '最后登录IP',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

INSERT INTO `sys_role` (`id`, `name`, `code`, `description`, `status`, `permissions`) VALUES
(1, '超级管理员', 'admin', '系统最高权限', 1, 'user:manage,role:manage,sensitive_word:manage,audit:manage'),
(2, '内容审核员', 'auditor', '内容审核权限', 1, 'audit:manage,sensitive_word:view'),
(3, '普通用户', 'user', '普通用户权限', 1, '');

INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `role_id`, `status`) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', '超级管理员', 'admin@example.com', 1, 1);