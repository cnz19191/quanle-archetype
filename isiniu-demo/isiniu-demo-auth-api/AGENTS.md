# isiniu-demo-auth-api

鉴权领域的公开契约。

- 只放 Service 接口、Request、Response、枚举、ErrorCodeEnum
- 只依赖 kernel
- 禁止放 Controller、Repository、Mapper、Model、BO、Convert、实现类
- 其他领域需要登录用户时用 `LoginUserHolder`，不要依赖本模块或 auth 实现

完整规约见仓库根目录 `AGENTS.md`。
