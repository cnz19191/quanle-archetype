# ${rootArtifactId}

单模块服务。适合小工具和内部服务。分层和示例与公司标杆一致，公共能力来自 `com.isiniu:isiniu-common`。

## 本地运行

先在公司脚手架仓库执行 `mvn install`。然后：

```bash
mvn verify
export AUTH_JWT_SECRET='至少32字节的本地密钥'
mvn spring-boot:run
```

环境变量：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`AUTH_JWT_SECRET`、`AUTH_MODE`、`CORS_ALLOWED_ORIGINS`。

表结构由 Flyway 管理，脚本在 `src/main/resources/db/migration/`，启动时自动建表。

鉴权两种模式，由 `isiniu.auth.mode` 切换：`local` 为账号密码登录并签发 JWT，`sso` 为校验外部 OIDC JWT。除白名单外都要带 `Authorization: Bearer <accessToken>`。跨域默认 `*`，**上线前必须改成具体域名**。

测试使用 Mockito 和 H2，不连接 MySQL。

领域变多、需要隔开 Mapper 和 Model 时，改用模块化单体模板。
