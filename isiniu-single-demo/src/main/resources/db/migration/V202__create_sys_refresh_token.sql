-- 刷新令牌表。只存 SHA-256 哈希，刷新一次轮换一次。
CREATE TABLE IF NOT EXISTS sys_refresh_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP${updateTimeOnUpdate},
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_sys_refresh_token_hash UNIQUE (token_hash)
)${tableOptions};

CREATE INDEX idx_sys_refresh_token_user ON sys_refresh_token (user_id, revoked);
