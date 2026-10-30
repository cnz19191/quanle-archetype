# isiniu-demo-comment-api

评论领域的公开契约，用来演示跨领域调用。

- 只放 Service 接口、Request、Response、枚举、ErrorCodeEnum
- 只依赖 kernel
- 禁止依赖 issue、auth 等任何领域的实现模块
- 其他领域要读评论，只能依赖本模块

完整规约见仓库根目录 `AGENTS.md`。
