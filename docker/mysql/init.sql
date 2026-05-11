-- 后台管理子系统 - 数据库初始化脚本

CREATE TABLE IF NOT EXISTS `user` (
    `object_id` VARCHAR(36) PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `nickname` VARCHAR(50),
    `email` VARCHAR(100),
    `user_type` ENUM('ADMIN','KNOWLEDGE_SERVICE','MOBILE') NOT NULL DEFAULT 'ADMIN',
    `status` ENUM('ENABLED','DISABLED') NOT NULL DEFAULT 'ENABLED',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 后续可在这里添加更多表：role, permission, audit_queue, artifact 等