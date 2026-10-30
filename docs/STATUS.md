# 状态

脚手架已包含：

- `isiniu-common`：统一返回、分页、异常、traceId、操作日志、Spring Security 双模式鉴权、跨域、MyBatis 填充和逻辑删除配置
- `isiniu-demo`：模块化单体标杆，鉴权 / 问题 / 评论三个领域
- `isiniu-single-demo`：单模块标杆
- `isiniu-archetype-modular`、`isiniu-archetype-single`

表结构由 Flyway 管理，测试使用 Mockito 和 H2，不连接 MySQL。鉴权支持内置账号密码（JWT）和 SSO（OIDC Resource Server），由 `isiniu.auth.mode` 切换。

Spring Boot 已升级到 4.1.2（Spring Framework 7.0.10、Spring Security 7.1.2、Jackson 3）。3.5 线已于 2026-06-30 EOL，不再作为基线。
