# isiniu Java 后端脚手架

公司级后端起点。规范只看根目录 [AGENTS.md](AGENTS.md)。公共能力打成 jar，业务项目不要复制 `isiniu-common` 的源码。

## 模块

| 模块 | 作用 |
|---|---|
| `isiniu-parent` | 父 POM，统一 Java 25、Spring Boot 3.5.16、MyBatis Plus 3.5.18.1、Spring Security、Flyway、Spotless |
| `isiniu-common` | 统一返回、分页、异常、traceId、操作日志、`LoginUserHolder`、跨域、MyBatis 填充、逻辑删除配置；不依赖 Spring Security |
| `isiniu-common-auth` | 鉴权：Spring Security 自动配置、LOCAL JWT、SSO 资源服务器，只由 auth 领域和启动模块依赖 |
| `isiniu-demo` | 模块化单体标杆：kernel、web、auth-api、auth、issue-api、issue、comment-api、comment、portal、bootstrap |
| `isiniu-single-demo` | 单模块标杆，同一套分层，含鉴权和问题管理示例 |
| `isiniu-archetype-modular` | 生成模块化单体 |
| `isiniu-archetype-single` | 生成单模块服务 |

`isiniu-demo` 的依赖方向：领域实现只依赖 kernel、本领域 api，以及**其他领域的 api**（`comment` 依赖 `issue-api` 就是示例）；portal 只依赖 api，编译期看不到 Repository / Mapper / Model；bootstrap 用 runtime 把实现组装进来。Maven enforcer 卡住跨领域实现依赖，ArchUnit 检查分层。

## 本地构建

需要 JDK 25 和 Maven 3.8+。Spring Boot 3.5.16 支持到 Java 25。为了在 JDK 25 上编译和检查格式：Lombok 使用 1.18.46（与 Boot 3.5.16 BOM 一致；1.18.38 在 JDK 25 上会让 javac 失败），ArchUnit 使用 1.4.2（1.4.0 读不了 Java 25 的 class 文件），Palantir Java Format 使用 2.71.0（2.50.0 在 JDK 25 上调用已删除的 javac API）。

本仓库自带 `./mvnw`，优先复用本机已有的项目 Maven 和仓库，不读取全局 `M2_HOME` / `mvn`：

```bash
./mvnw -version
./mvnw -B spotless:apply
./mvnw -B verify
```

本机路径写在 `.mvn/local-env`（已加入 `.gitignore`，不提交）：

```bash
LOCAL_MAVEN_HOME=/path/to/apache-maven-3.9
LOCAL_MAVEN_REPO=/path/to/maven-repository
```

没有这个文件时，`./mvnw` 会下载 Maven 3.9.9 到 `~/.m2/wrapper/dists`，并把依赖装进项目内的 `.mvn/repository`，方便新环境直接跑。IDEA 可以继续用它自己配置的 Maven，只要 local repository 和这里一致；也可以切到 “Use Maven wrapper”。

测试不连接 MySQL。Service 用 Mockito，数据库用 H2 的 MySQL 模式。生产库仍然是 MySQL 8，表结构由 Flyway 管理，脚本在各模块的 `src/main/resources/db/migration/`，命名规范见 `AGENTS.md` 第 8.4 节。

## 安装 Archetype

不配置私服，先在本地验证：

```bash
# 1. 安装 parent、common 和两个 archetype 到本地仓库
./mvnw -B clean install

# 2. 生成模块化项目和单模块项目（命令见下文）

# 3. 两个生成项目分别执行
mvn verify
```

`./mvnw install` 会把 `isiniu-parent`、`isiniu-common`、`isiniu-common-auth` 和两个 archetype 装进本地仓库。新项目的父 POM 是 `com.isiniu:isiniu-parent`，所以生成出的工程要能解析到这次安装的版本。

私服地址暂未配置。根 `pom.xml` 里保留了注释掉的 `distributionManagement` 占位，等私服就绪后再打开并补上地址。

## 生成模块化单体

适合正式业务。带 kernel、web、portal、bootstrap，以及一个可复制的问题管理领域。

```bash
./mvnw -B archetype:generate \
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
./mvnw -B archetype:generate \
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

数据库账号、JWT 密钥都走环境变量，不要写进配置文件：

```bash
export DB_URL='jdbc:mysql://127.0.0.1:3306/isiniu_demo?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true'
export DB_USERNAME=isiniu
export DB_PASSWORD=
export AUTH_JWT_SECRET='至少32字节的本地密钥，生产从配置中心读取'
./mvnw -pl isiniu-demo-bootstrap -am spring-boot:run
```

启动时 Flyway 自动建表。本地验证可以插一个测试账号（密码是 `password`，**只在本地用**）。客户端连接请显式使用 `utf8mb4`（例如 `mysql --default-character-set=utf8mb4 ...`），否则中文会乱码：

```sql
INSERT INTO sys_user (username, password_hash, display_name, status, create_time, update_time, deleted)
VALUES ('tester', '$2a$10$Ap5Z2Jbl1CwgqcEC29p/sOE05JyveLFCT3/D2/ftX9UuihuZagsPm', '测试账号', 1, NOW(), NOW(), 0);
```

然后登录拿 token：

```bash
curl -s -X POST http://127.0.0.1:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"tester","password":"password"}'
```

后续请求带 `Authorization: Bearer <accessToken>`。接口文档在 `/swagger-ui.html`。

### 切换 SSO 模式

```bash
export AUTH_MODE=sso
# 必填：令牌的 iss 必须与它一致
export AUTH_SSO_ISSUER_URI='https://idp.example.com/realms/isiniu'
# 必填：本应用的 client_id / audience，令牌的 aud 必须包含其中一个，多个用逗号分隔
export AUTH_SSO_AUDIENCES='isiniu-demo'
# 可选：企业微信、钉钉这类没有发现端点的 IdP，直接指定公钥地址
export AUTH_SSO_JWK_SET_URI='https://idp.example.com/.well-known/jwks.json'
```

`issuer-uri` 或 `audiences` 未配置时应用拒绝启动，避免接受同一 IdP 签给其他应用的令牌。

SSO 模式下不提供账号密码登录，只校验外部 OIDC 的 JWT。用户通过 `sys_user.external_id` 绑定，`isiniu.auth.sso.user-id-claim` 决定用哪个 claim 作为主体（默认 `sub`），`auto-provision` 默认关闭。

单模块标杆：

```bash
./mvnw -pl isiniu-single-demo spring-boot:run
```

## 改完标杆后同步模板

Archetype 资源由脚本从两个标杆生成，不手改 `archetype-resources`。

```bash
python3 scripts/sync-archetypes.py
```

`./mvnw verify` 和 `./mvnw install` 也会在打包 archetype 前自动跑这个脚本，但**不会**生成并构建示例项目。改完标杆后，在 `./mvnw -B clean install` 之后单独执行：

```bash
bash scripts/verify-archetypes.sh
```

它用两个 archetype 各生成一个项目到 `/tmp/isiniu-archetype-out` 并执行 `verify`，CI 中也会运行。
