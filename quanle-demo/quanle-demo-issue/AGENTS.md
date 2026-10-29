# quanle-demo-issue

问题领域实现，标杆模块。新领域复制 `quanle-demo-issue-api` 和本模块。

内部包结构：

```
com.quanle.demo.issue
├── controller
├── service.impl
├── repository / repository.impl
├── mapper
├── model
├── dto.bo
├── convert
└── client
```

- 编译期只依赖 kernel 和 `quanle-demo-issue-api`
- 禁止依赖 web、portal、bootstrap 以及其他领域的实现模块
- Service 不直接调用 Mapper；Controller 不注入 Repository

完整规约见仓库根目录 `AGENTS.md`。
