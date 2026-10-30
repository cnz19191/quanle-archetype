-- 评论表。跨领域示例：评论只通过 issue_id 关联，不复制问题字段。
CREATE TABLE IF NOT EXISTS comment_main (
    id BIGINT NOT NULL AUTO_INCREMENT,
    issue_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    author_id BIGINT NOT NULL,
    biz_no VARCHAR(64) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP${updateTimeOnUpdate},
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_comment_issue_biz_no UNIQUE (issue_id, biz_no)
)${tableOptions};

CREATE INDEX idx_comment_issue_create_time ON comment_main (issue_id, create_time, id);
