-- 问题主表。索引服务分页和幂等，执行前需人工确认。
CREATE TABLE IF NOT EXISTS issue_main (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title       VARCHAR(128) NOT NULL COMMENT '标题',
    content     VARCHAR(2000)         DEFAULT NULL COMMENT '内容',
    status      TINYINT      NOT NULL COMMENT '状态：0待处理 1处理中 2已关闭',
    reporter_id BIGINT       NOT NULL COMMENT '提交人',
    biz_no      VARCHAR(64)  NOT NULL COMMENT '业务幂等号',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_issue_biz_no (biz_no),
    KEY idx_issue_reporter_update_time (reporter_id, update_time, id),
    KEY idx_issue_reporter_status_update_time (reporter_id, status, update_time, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '问题主表';
