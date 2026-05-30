CREATE TABLE IF NOT EXISTS `user` (
    `object_id` VARCHAR(64) PRIMARY KEY,
    `username` VARCHAR(100) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(100) DEFAULT '',
    `email` VARCHAR(200) DEFAULT '',
    `phone` VARCHAR(20) DEFAULT '',
    `avatar` VARCHAR(500) DEFAULT '',
    `user_type` VARCHAR(50) DEFAULT 'ADMIN',
    `status` VARCHAR(20) DEFAULT 'ENABLED',
    `last_login_time` DATETIME NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT DEFAULT 0,
    INDEX `idx_user_username` (`username`),
    INDEX `idx_user_status` (`status`),
    INDEX `idx_user_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `role` (
    `object_id` VARCHAR(64) PRIMARY KEY,
    `role_name` VARCHAR(100) NOT NULL,
    `role_code` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(500) DEFAULT '',
    `permissions` TEXT,
    `is_system` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `audit_queue` (
    `object_id` VARCHAR(64) PRIMARY KEY,
    `content_type` VARCHAR(50) DEFAULT '',
    `content_text` TEXT,
    `content_url` VARCHAR(500) DEFAULT '',
    `author_id` VARCHAR(64) DEFAULT '',
    `auto_audit_result` VARCHAR(20) DEFAULT 'MANUAL',
    `auto_audit_detail` VARCHAR(500) DEFAULT '',
    `status` VARCHAR(20) DEFAULT 'PENDING',
    `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `audit_user_id` VARCHAR(64) DEFAULT '',
    `audit_time` DATETIME NULL,
    `audit_remark` VARCHAR(500) DEFAULT '',
    `reject_reason` VARCHAR(500) DEFAULT '',
    INDEX `idx_audit_type` (`content_type`),
    INDEX `idx_audit_status` (`status`),
    INDEX `idx_audit_submit_time` (`submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `role` (`object_id`, `role_name`, `role_code`, `description`, `permissions`, `is_system`)
VALUES
('role-admin', 'Super Admin', 'ADMIN', 'Full system management permissions', 'dashboard:view,user:read,user:write,role:read,role:write,audit:read,audit:write,data:read,data:write,backup:read,backup:write,log:read,settings:read,settings:write', 1),
('role-auditor', 'Content Auditor', 'AUDITOR', 'Content review permissions', 'audit:read,audit:write', 1),
('role-user', 'Normal User', 'USER', 'Default ordinary user role', 'profile:read', 1)
ON DUPLICATE KEY UPDATE
    `role_name` = VALUES(`role_name`),
    `description` = VALUES(`description`),
    `permissions` = VALUES(`permissions`),
    `is_system` = VALUES(`is_system`);

INSERT INTO `user` (`object_id`, `username`, `password_hash`, `nickname`, `email`, `phone`, `user_type`, `status`, `is_deleted`)
VALUES
('admin-user-1', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'Admin', 'admin@example.com', '13800000000', 'ADMIN', 'ENABLED', 0)
ON DUPLICATE KEY UPDATE
    `nickname` = VALUES(`nickname`),
    `email` = VALUES(`email`),
    `phone` = VALUES(`phone`),
    `status` = VALUES(`status`),
    `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `audit_queue` (`object_id`, `content_type`, `content_text`, `author_id`, `auto_audit_result`, `auto_audit_detail`, `status`, `submit_time`)
VALUES
('audit-sample-1', 'TEXT', 'Sample narration content waiting for manual review.', 'admin-user-1', 'MANUAL', 'Manual review rule matched', 'PENDING', NOW()),
('audit-sample-2', 'COMMENT', 'Sample user comment. Auto audit suggests approve.', 'admin-user-1', 'PASS', 'Auto audit passed', 'PENDING', NOW()),
('audit-sample-3', 'TEXT', 'Sample content with suspicious wording for review.', 'admin-user-1', 'MANUAL', 'Possible sensitive word detected', 'PENDING', NOW())
ON DUPLICATE KEY UPDATE
    `content_text` = VALUES(`content_text`),
    `auto_audit_result` = VALUES(`auto_audit_result`),
    `auto_audit_detail` = VALUES(`auto_audit_detail`),
    `status` = VALUES(`status`);
