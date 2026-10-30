# isiniu Java 后端脚手架

公司级后端起点。规范只看根目录 [AGENTS.md](AGENTS.md)。公共能力打成 jar，业务项目不要复制 `isiniu-common` 的源码。

## 模块

| 模块 | 作用 |
|---|---|
| `isiniu-parent` | 父 POM，统一 Java 21、Spring Boot 3.2.12、MyBatis Plus 3.5.18.1、Spotless |
| `isiniu-common` | 统一返回、分页、异常、traceId、操作日志、MyBatis 填充、逻辑删除配置 |
| `isiniu-demo` | 模块化单体标杆：kernel、web、issue-api、issue、portal、bootstrap |
| `isiniu-single-demo` | 单模块标杆，同一套分层和问题管理示例 |
| `isiniu-archetype-modular` | 生成模块化单体 |
| `isiniu-archetype-single` | 生成单模块服务 |

`isiniu-demo` 的依赖方向：领域实现只依赖 kernel 和本领域 api；portal 只依赖 api，编译期看不到 Repository / Mapper / Model；bootstrap 用 runtime 把实现组装进来。Maven enforcer 卡住跨领域实现依赖，ArchUnit 检查分层。

## 本地构建

需要 JDK 21 和 Maven 3.8+。

```bash
mvn verify
```

测试不连接 MySQL。Service 用 Mockito，Mapper XML 用 H2 的 MySQL 模式。生产库仍然是 MySQL 8，建表脚本在 `isiniu-demo/docs/sql/` 和 `isiniu-single-demo/docs/sql/`，确认后再执行。

## 安装 Archetype

```bash
mvn install
```

这会把 `isiniu-parent`、`isiniu-common` 和两个 archetype 装进本地仓库。新项目的父 POM 是 `com.isiniu:isiniu-parent`，所以生成出的工程要能解析到这次安装的版本（本地仓库或公司私服）。

## 生成模块化单体

适合正式业务。带 kernel、web、portal、bootstrap，以及一个可复制的问题管理领域。

```bash
mvn archetype:generate -B \
  -DarchetypeGroupId=com.isiniu \
  -DarchetypeArtifactId=isiniu-archetype-modular \
  -DarchetypeVersion=1.0.0-SNAPSHOT \
  -DgroupId=com.example \
  -DartifactId=sample-service \
  -Dversion=1.0.0-SNAPSHOT \
  -Dpackage=com.example.sample \
  -Dauthor=isiniu \
  -DerrorCodePrefix=SMP \
  -DoutputDirectory=/tmp/generated
```

生成结束后会自动执行一次 `mvn spotless:apply`，让 import 顺序符合新包名。然后：

```bash
cd /tmp/generated/sample-service
mvn verify
```

新增领域时复制 `sample-service-issue-api` 和 `sample-service-issue`，步骤写在生成项目的 `AGENTS.md` 第 3.4 节。

## 生成单模块服务

适合小工具和内部服务。分层、示例和门禁与标杆相同，不拆领域模块。

```bash
mvn archetype:generate -B \
  -DarchetypeGroupId=com.isiniu \
  -DarchetypeArtifactId=isiniu-archetype-single \
  -DarchetypeVersion=1.0.0-SNAPSHOT \
  -DgroupId=com.example \
  -DartifactId=sample-tool \
  -Dversion=1.0.0-SNAPSHOT \
  -Dpackage=com.example.tool \
  -Dauthor=isiniu \
  -DerrorCodePrefix=SMP \
  -DoutputDirectory=/tmp/generated
```

生成后同样会执行 `mvn spotless:apply`。

```bash
cd /tmp/generated/sample-tool
mvn verify
```

## 运行标杆

数据库账号密码走环境变量，不要写进配置文件：

```bash
export DB_URL='jdbc:mysql://127.0.0.1:3306/isiniu_demo?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true'
export DB_USERNAME=isiniu
export DB_PASSWORD=
mvn -pl isiniu-demo-bootstrap -am spring-boot:run
```

登录是占位拦截器，请求头 `X-User-Id` 传用户 ID。生产要换成统一认证。接口文档在 `/swagger-ui.html`。

单模块标杆：

```bash
mvn -pl isiniu-single-demo spring-boot:run
```

## 改完标杆后同步模板

Archetype 资源由脚本从两个标杆生成，不手改 `archetype-resources`。

```bash
python3 scripts/sync-archetypes.py
```

`mvn verify` 和 `mvn install` 也会在打包 archetype 前自动跑这个脚本。
