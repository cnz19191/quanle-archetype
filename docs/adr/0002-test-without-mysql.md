# 0002 测试不连接真实 MySQL

## 状态

已接受

## 决定

- 业务规则用 Mockito 测 Service。
- Mapper XML、逻辑删除、唯一索引用 H2 的 MySQL 模式。
- 不在 `mvn verify` 里启动 MySQL 或 Testcontainers，避免 CI 依赖数据库。

## 后果

H2 不支持 `ON UPDATE CURRENT_TIMESTAMP`。手写 XML 更新状态的集成测试只断言影响行数和状态值，不断言数据库自动刷新的 `update_time`。生产 DDL 仍然使用该列定义。
