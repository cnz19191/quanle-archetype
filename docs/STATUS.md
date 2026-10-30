# 状态

脚手架已包含：

- `isiniu-common`：统一返回、分页、异常、traceId、操作日志、MyBatis 填充和逻辑删除配置
- `isiniu-demo`：模块化单体标杆，问题管理
- `isiniu-single-demo`：单模块标杆
- `isiniu-archetype-modular`、`isiniu-archetype-single`

测试使用 Mockito 和 H2，不连接 MySQL。登录仍是 `X-User-Id` 占位，上线前要换成统一认证。
