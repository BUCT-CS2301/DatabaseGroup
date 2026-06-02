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

-- =========================================================
-- Mobile API required extra table: user_favorite
-- Used by favorite group summary, add favorite, update favorite group, delete favorite
-- =========================================================
CREATE TABLE IF NOT EXISTS user_favorite (
    object_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    artifact_id VARCHAR(36) NOT NULL,
    group_name VARCHAR(100) NOT NULL DEFAULT 'default',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_artifact (user_id, artifact_id),
    KEY idx_user_favorite_user_id (user_id),
    KEY idx_user_favorite_artifact_id (artifact_id),
    KEY idx_user_favorite_group (user_id, group_name)
);

-- =========================================================
-- Mobile API required columns for artifact search and favorite response
-- image_url and image_path may be missing in some local initialized databases
-- The following statements are safe to run repeatedly.
-- =========================================================
SET @sql_image_url = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE artifact ADD COLUMN image_url VARCHAR(1000) NULL',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'artifact'
      AND column_name = 'image_url'
);

PREPARE stmt_image_url FROM @sql_image_url;
EXECUTE stmt_image_url;
DEALLOCATE PREPARE stmt_image_url;

SET @sql_image_path = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE artifact ADD COLUMN image_path VARCHAR(500) NULL',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'artifact'
      AND column_name = 'image_path'
);

PREPARE stmt_image_path FROM @sql_image_path;
EXECUTE stmt_image_path;
DEALLOCATE PREPARE stmt_image_path;

-- =========================================================
-- Mobile profile API required column: user.bio
-- Used by current-user and edit-profile APIs.
-- =========================================================
SET @sql_user_bio = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE user ADD COLUMN bio VARCHAR(500) NULL',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user'
      AND column_name = 'bio'
);

PREPARE stmt_user_bio FROM @sql_user_bio;
EXECUTE stmt_user_bio;
DEALLOCATE PREPARE stmt_user_bio;