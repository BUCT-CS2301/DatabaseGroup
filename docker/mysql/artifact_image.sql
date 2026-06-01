-- 文物多图元数据（须在 init.sql 创建 artifact 表之后执行）
-- Docker 挂载为 /docker-entrypoint-initdb.d/02-artifact_image.sql

USE `admin_platform`;

CREATE TABLE IF NOT EXISTS `artifact_image` (
    `file_name`   VARCHAR(64)  NOT NULL COMMENT '落盘文件名（UUID v7 + 扩展名）',
    `artifact_id` VARCHAR(36)  NOT NULL COMMENT '所属文物 object_id',
    PRIMARY KEY (`file_name`),
    INDEX `idx_artifact_id` (`artifact_id`),
    CONSTRAINT `fk_artifact_image_artifact`
        FOREIGN KEY (`artifact_id`) REFERENCES `artifact`(`object_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文物图片文件索引';
