# quanle-demo-bootstrap

启动模块。

- 放启动类、`application.yml`、Dockerfile、ArchUnit
- 依赖 web、portal、kernel
- 领域实现模块使用 runtime 依赖，避免启动类在编译期引用 Repository
- 新增领域时，在这里加上该领域实现模块的 runtime 依赖

完整规约见仓库根目录 `AGENTS.md`。
