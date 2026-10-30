-- 问题主表。MySQL 表选项通过 Flyway placeholder 注入，H2 测试环境为空。
CREATE TABLE IF NOT EXISTS issue_main (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    content VARCHAR(2000),
    status TINYINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    biz_no VARCHAR(64) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP${updateTimeOnUpdate},
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_issue_biz_no UNIQUE (biz_no)
)${tableOptions};

CREATE INDEX idx_issue_reporter_update_time ON issue_main (reporter_id, update_time, id);
CREATE INDEX idx_issue_reporter_status_update_time ON issue_main (reporter_id, status, update_time, id);
