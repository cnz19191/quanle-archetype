# 问题管理技术方案

## 落点

领域实现 `*-issue`，公开契约 `*-issue-api`。门户只调用 `IssueService`。

## 接口

| 方法 | 路径 | 登录 | 说明 |
|---|---|---|---|
| POST | `/api/issues` | 是 | 新建，`bizNo` 幂等 |
| GET | `/api/issues` | 是 | 分页，只返回当前用户 |
| PUT | `/api/issues/{id}/status` | 是 | 状态条件更新 |
| GET | `/api/portal/issues` | 是 | 门户总览 |

## 表和索引

脚本见 `docs/sql/V1__issue_main.sql`。

- `uk_issue_biz_no`：幂等
- `idx_issue_reporter_update_time`：默认分页，过滤提交人并按更新时间排序
- `idx_issue_reporter_status_update_time`：带状态筛选的分页

标题模糊查询带前导百分号，普通 BTree 用不上。标杆数据量可控；数据变大后要改精确查询或搜索引擎，需另开方案。

## 事务

`createIssue` 和 `changeStatus` 是事务方法，里面只有数据库操作。撞上唯一索引时抛 `QLE0004`，调用方重试后走幂等查询。
