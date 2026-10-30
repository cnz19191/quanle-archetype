# ${rootArtifactId}

模块化单体。公共能力来自 `com.isiniu:isiniu-common`，不要复制它的源码。

## 模块

| 模块 | 作用 |
|---|---|
| `${rootArtifactId}-kernel` | 错误码号段等项目级约定 |
| `${rootArtifactId}-web` | 跨域、异步线程池、健康检查 |
| `${rootArtifactId}-auth-api` | 鉴权领域公开契约 |
| `${rootArtifactId}-auth` | 登录、令牌刷新、SSO 用户绑定 |
| `${rootArtifactId}-issue-api` | 问题领域公开契约，其他领域只能依赖它 |
| `${rootArtifactId}-issue` | 问题领域实现，标杆。新领域照它复制 |
| `${rootArtifactId}-comment-api` | 评论领域公开契约 |
| `${rootArtifactId}-comment` | 评论实现，演示跨领域只依赖 issue-api |
| `${rootArtifactId}-portal` | 跨领域聚合 |
| `${rootArtifactId}-bootstrap` | 启动、配置、打包 |

## 本地运行

先在公司脚手架仓库执行 `mvn install`，保证父 POM 和 `isiniu-common` 在本地仓库。然后：

```bash
mvn verify
export AUTH_JWT_SECRET='至少32字节的本地密钥'
mvn -pl ${rootArtifactId}-bootstrap -am spring-boot:run
```

环境变量：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`AUTH_JWT_SECRET`、`AUTH_MODE`、`CORS_ALLOWED_ORIGINS`。

表结构由 Flyway 管理，脚本在各模块的 `src/main/resources/db/migration/`。启动时自动建表，命名规范见根目录 `AGENTS.md` 第 8.4 节。

鉴权两种模式，由 `isiniu.auth.mode` 切换：

- `local`：`POST /api/auth/login` 账号密码登录，BCrypt 校验，签发 access token + refresh token。
- `sso`：配置 `isiniu.auth.sso.issuer-uri` 或 `jwk-set-uri`，校验外部 OIDC JWT，用户按 `sys_user.external_id` 绑定。

除白名单外，所有接口都要带 `Authorization: Bearer <accessToken>`。跨域默认 `*`，**上线前必须改成具体域名**。

测试使用 Mockito 和 H2，不连接 MySQL。

## 新增领域

按根目录 `AGENTS.md` 第 3.4 节：复制 issue 的 api 和实现模块，登记父 POM、号段、portal 依赖和 bootstrap 的 runtime 依赖。其他领域只能依赖新领域的 `*-api`。
