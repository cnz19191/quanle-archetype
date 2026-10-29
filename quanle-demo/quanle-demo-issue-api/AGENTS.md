# quanle-demo-issue-api

问题领域的公开契约。

- 只放 Service 接口、Request、Response、枚举、ErrorCodeEnum
- 只依赖 kernel
- 禁止放 Controller、Repository、Mapper、Model、BO、Convert、实现类
- 其他领域如果要调用问题领域，只能依赖本模块，不能依赖 `quanle-demo-issue`

完整规约见仓库根目录 `AGENTS.md`。
