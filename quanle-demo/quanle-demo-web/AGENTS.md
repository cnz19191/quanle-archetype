# quanle-demo-web

Web 公共配置：登录拦截器注册、跨域、异步线程池、健康检查。

- 只依赖 kernel（以及 Spring Web）
- 全局异常处理器、traceId 过滤器、接口日志和操作日志来自 `quanle-common`，本模块不要再声明一份
- 禁止依赖领域模块。领域 Controller 不能为了使用拦截器而依赖本模块；登录注解在 `quanle-common`

完整规约见仓库根目录 `AGENTS.md`。
