-- 评论、点赞、收藏、浏览历史扩展表
-- Docker 挂载为 /docker-entrypoint-initdb.d/03-interaction_tables.sql

USE `admin_platform`;

CREATE TABLE IF NOT EXISTS `comment_like` (
    `object_id`   VARCHAR(36) PRIMARY KEY,
    `comment_id`  VARCHAR(36) NOT NULL,
    `user_id`     VARCHAR(36) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    INDEX `idx_comment_like_comment` (`comment_id`),
    CONSTRAINT `fk_comment_like_comment`
        FOREIGN KEY (`comment_id`) REFERENCES `ugc_comment`(`object_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_like_user`
        FOREIGN KEY (`user_id`) REFERENCES `user`(`object_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `artifact_like` (
    `object_id`   VARCHAR(36) PRIMARY KEY,
    `artifact_id` VARCHAR(36) NOT NULL,
    `user_id`     VARCHAR(36) NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_artifact_user` (`artifact_id`, `user_id`),
    INDEX `idx_artifact_like_user_time` (`user_id`, `create_time`),
    CONSTRAINT `fk_artifact_like_artifact`
        FOREIGN KEY (`artifact_id`) REFERENCES `artifact`(`object_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_artifact_like_user`
        FOREIGN KEY (`user_id`) REFERENCES `user`(`object_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_favorite` (
    `object_id`   VARCHAR(36) PRIMARY KEY,
    `user_id`     VARCHAR(36) NOT NULL,
    `artifact_id` VARCHAR(36) NOT NULL,
    `group_name`  VARCHAR(100) DEFAULT 'default',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_artifact` (`user_id`, `artifact_id`),
    INDEX `idx_user_favorite_time` (`user_id`, `create_time`),
    CONSTRAINT `fk_user_favorite_user`
        FOREIGN KEY (`user_id`) REFERENCES `user`(`object_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_favorite_artifact`
        FOREIGN KEY (`artifact_id`) REFERENCES `artifact`(`object_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_browse_history` (
    `object_id`   VARCHAR(36) PRIMARY KEY,
    `user_id`     VARCHAR(36) NOT NULL,
    `artifact_id` VARCHAR(36) NOT NULL,
    `browse_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_browse` (`user_id`, `browse_time`),
    CONSTRAINT `fk_user_browse_user`
        FOREIGN KEY (`user_id`) REFERENCES `user`(`object_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_browse_artifact`
        FOREIGN KEY (`artifact_id`) REFERENCES `artifact`(`object_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
