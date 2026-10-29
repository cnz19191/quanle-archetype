# ${rootArtifactId}

单模块服务。适合小工具和内部服务。分层和问题管理示例与公司标杆一致，公共能力来自 `com.quanle:quanle-common`。

## 本地运行

先在公司脚手架仓库执行 `mvn install`。然后：

```bash
mvn verify
mvn spring-boot:run
```

环境变量：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`CORS_ALLOWED_ORIGINS`。建表脚本在 `docs/sql/`。

测试使用 Mockito 和 H2，不连接 MySQL。

领域变多、需要隔开 Mapper 和 Model 时，改用模块化单体模板。
