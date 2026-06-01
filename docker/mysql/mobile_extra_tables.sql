CREATE TABLE IF NOT EXISTS user_favorite_group (
    object_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_group (user_id, group_name)
);

CREATE TABLE IF NOT EXISTS user_privacy_setting (
    object_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    favorites_visible TINYINT NOT NULL DEFAULT 1,
    likes_visible TINYINT NOT NULL DEFAULT 1,
    comments_visible TINYINT NOT NULL DEFAULT 1,
    uploads_visible TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_privacy (user_id)
);