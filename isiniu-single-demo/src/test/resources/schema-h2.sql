DROP TABLE IF EXISTS issue_main;
CREATE TABLE issue_main (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    content VARCHAR(2000),
    status INT NOT NULL,
    reporter_id BIGINT NOT NULL,
    biz_no VARCHAR(64) NOT NULL,
    create_time TIMESTAMP NOT NULL,
    update_time TIMESTAMP NOT NULL,
    deleted INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_issue_biz_no UNIQUE (biz_no)
);
