# isiniu-demo-web

Web 公共配置：跨域、异步线程池、健康检查。

- 只依赖 kernel（以及 Spring Web）
- 鉴权、跨域、全局异常、traceId、接口日志来自 `isiniu-common` 的自动配置，本模块不要再声明一份
- 禁止依赖领域模块
- 新增免登录路径改 `isiniu.auth.permit-paths`，不要改代码

完整规约见仓库根目录 `AGENTS.md`。
