# SQL

建表脚本由 Flyway 管理，唯一来源是各模块的 `src/main/resources/db/migration/`，本目录不再存放可执行脚本。

- 命名：`V{版本}__{描述}.sql`，例如 `V101__create_issue_main.sql`。
- 版本号按领域分块：问题 1xx、鉴权 2xx、评论 3xx，新领域按顺序申请下一段百号。
- 已经执行过的脚本禁止修改，只能追加新版本。
- MySQL 专有表选项（`ENGINE`、`CHARSET`、`ON UPDATE`）通过 Flyway placeholder 注入，保证 H2 测试能执行同一份脚本。
- 表结构和索引仍需先写进 `docs/design/` 技术方案，确认后再提交迁移脚本。
