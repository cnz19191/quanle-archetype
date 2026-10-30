# isiniu-demo-portal

面向前端的跨领域聚合。

- 编译期只依赖 kernel 和各领域的 `*-api`
- 禁止依赖领域实现模块，这样看不到 Repository、Mapper、Model
- 单一领域的增删改查放在领域模块；这里只做组装

新增领域后，在本模块增加对 `{领域}-api` 的依赖，并在 enforcer 里禁止依赖该领域的实现模块。

完整规约见仓库根目录 `AGENTS.md`。
