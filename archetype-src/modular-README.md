# ${rootArtifactId}

模块化单体。公共能力来自 `com.quanle:quanle-common`，不要复制它的源码。

## 模块

| 模块 | 作用 |
|---|---|
| `${rootArtifactId}-kernel` | 错误码号段等项目级约定 |
| `${rootArtifactId}-web` | 登录拦截器、跨域、异步线程池 |
| `${rootArtifactId}-issue-api` | 问题领域公开契约，其他领域只能依赖它 |
| `${rootArtifactId}-issue` | 问题领域实现，标杆。新领域照它复制 |
| `${rootArtifactId}-portal` | 跨领域聚合 |
| `${rootArtifactId}-bootstrap` | 启动、配置、打包 |

## 本地运行

先在公司脚手架仓库执行 `mvn install`，保证父 POM 和 `quanle-common` 在本地仓库。然后：

```bash
mvn verify
mvn -pl ${rootArtifactId}-bootstrap -am spring-boot:run
```

环境变量：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`CORS_ALLOWED_ORIGINS`。建表脚本在 `docs/sql/`，确认后再执行。

测试使用 Mockito 和 H2，不连接 MySQL。

## 新增领域

按根目录 `AGENTS.md` 第 3.4 节：复制 issue 的 api 和实现模块，登记父 POM、号段、portal 依赖和 bootstrap 的 runtime 依赖。
