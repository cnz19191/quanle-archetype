# isiniu-demo-auth

鉴权领域实现：账号密码登录、令牌刷新、SSO 用户映射。

- 只依赖 kernel 和本领域 api，以及 Spring Security、MyBatis Plus、Flyway
- 用户表和刷新令牌表的迁移脚本在本模块 `src/main/resources/db/migration`
- 不依赖 web、portal、bootstrap 和其他领域实现
- 业务代码不要直接使用本模块的 Repository / Mapper / Model

完整规约见仓库根目录 `AGENTS.md`。
