# AGENTS.md：项目架构与编码规约

> 本文件是 AI 编码助手（Codex、Claude Code、Cursor）和团队成员共同遵守的**唯一规范来源**。
> - Codex、Cursor 直接读取本文件；Claude Code 通过根目录 `CLAUDE.md` 中的 `@AGENTS.md` 引用。
> - 各开发 skill 只描述流程，规范一律以本文件为准；两者冲突时以本文件为准。
> - 标注【项目配置】的内容每个项目需按实际填写。Archetype 生成时会替换占位符。
> - 子模块可以有自己的简短 `AGENTS.md`，只说明该模块边界；与本文件冲突时以本文件为准。

脚手架仓库中的标杆取值：根包 `com.isiniu.demo`（单模块标杆为 `com.isiniu.single`），作者 `isiniu`，错误码前缀 `ISN`，标杆领域 `auth`（鉴权）、`issue`（问题管理）、`comment`（评论）。

---

## 0. 项目配置【项目配置】

| 项 | 值 |
|---|---|
| 根包名 | `${package}` |
| 作者标识（`@author`） | `${author}` |
| 标杆模块 | `${artifactId}-issue`（问题管理，新代码照它写） |
| 错误码模块前缀 | `${errorCodePrefix}` |

下文用 `{base}` 指代根包名。模块化单体里，每个领域模块再把 `{base}.{领域名}` 当作自己的根包。

---

## 1. 给 AI 的工作守则（最高优先级）

1. **先读后写**：动手前先读本文件、`docs/` 下相关设计文档，以及标杆模块和将要修改的现有文件。
2. **先分析后动手**：新需求按「需求分析 → 技术方案 → 代码生成 → 自检」推进，前两个阶段输出后**必须等人确认**，不得直接写代码。
3. **小步切片**：功能点超过 3 个的需求，先按功能拆成若干切片，每个切片完成「代码 + 测试 + 自检」并经人确认后再做下一个。
4. **保持一致**：命名、注解、代码风格与项目现有代码完全一致；不顺手重构任务范围外的代码。
5. **不替人做决策**：以下情况必须停下来列出问题和备选方案，等人确认：
   - 需求不明确或有歧义
   - 新增第三方依赖
   - 新建表、修改表结构
   - 修改已有对外接口的入参或出参
   - 修改公共模块、公共基类、跨模块调用
6. **必须带测试**：新增或修改的业务逻辑附单元测试；修 bug 先写能复现的测试。
7. **完成即自检**：按第 12 节逐项检查，并输出新建和修改的文件清单。

---

## 2. 技术栈基线

| 类别 | 选型 |
|---|---|
| 语言 | Java 25 |
| 框架 | Spring Boot 4.1.2（Spring Framework 7.0、Jackson 3；import 一律用 `jakarta.*`，禁止 Java EE 的 `javax.*`） |
| 鉴权 | Spring Security 7.1，LOCAL / SSO 双模式。LOCAL 用 HS256 JWT，SSO 用 OAuth2 Resource Server 校验 `iss` 和 `aud` |
| 持久层 | MyBatis Plus 3.5.18（`mybatis-plus-spring-boot4-starter`）+ MyBatis XML |
| 数据库 | MySQL 8.x，表结构由 Flyway 管理（`spring-boot-starter-flyway`，MySQL 再加 `flyway-mysql`） |
| 对象转换 | MapStruct 1.6 |
| 工具类 | Hutool、Spring `CollectionUtils` / `StringUtils` |
| 接口文档 | SpringDoc 3.2（`@Tag`、`@Operation`、`@Schema`） |
| 测试 | JUnit 6 + Mockito。Web 测试使用 `spring-boot-starter-webmvc-test` |
| JSON | Jackson 3。注入 `tools.jackson.databind.json.JsonMapper`，不要注入 Jackson 2 的 `com.fasterxml.jackson.databind.ObjectMapper`。注解仍用 `com.fasterxml.jackson.annotation` |

**新增任何第三方依赖必须经人确认**，并说明理由。

Web 应用用 `spring-boot-starter-webmvc`，切面用 `spring-boot-starter-aspectj`。`application.yml` 保留 `spring.jackson.use-jackson2-defaults=true`，让字段顺序和日期格式与 Boot 3 对齐。

公司级公共能力来自 `com.isiniu:isiniu-common` 这个 jar，不要把 common 的源码复制进业务项目。鉴权（Spring Security、JWT、SSO）单独放在 `com.isiniu:isiniu-common-auth`，只有 auth 领域和启动模块依赖它；`isiniu-common` 不依赖 Spring Security。

---

## 3. 分层架构

### 3.1 调用链路（禁止跨层）

```
Controller → Service → Repository → Mapper
```

| 层 | 职责 | 可以注入 | 禁止 |
|---|---|---|---|
| Controller | 参数接收、校验、返回结果 | Service | 业务逻辑；注入 Repository / Mapper |
| Service | 业务逻辑、事务边界、对象转换（BO/Model → Response） | Repository、其他模块的 Service | 直接注入或调用 Mapper；Service 之间循环依赖 |
| Repository | 数据访问封装，继承 `MpBaseService<Model>` / `MpBaseServiceImpl<Mapper, Model>` | Mapper（通过 `getBaseMapper()`） | 业务逻辑 |
| Mapper | SQL 执行 | — | 返回 Response |

### 3.2 跨模块规则

- 跨领域只能调用对方 `*-api` 中 `api` 包的接口（如 `IssueQueryApi`），禁止直接使用对方的 Service、Repository、Mapper、Model、BO、Convert。门户（portal）作为编排层可以调用各领域的 `service` 接口。
- 允许：领域实现依赖其他领域的 `*-api`（`api` 接口、Request/Response、枚举、错误码）。
- 禁止：领域实现依赖其他领域的实现模块（`*-issue`、`*-comment` 等）。
- 示例：`{artifactId}-comment` 依赖 `{artifactId}-issue-api`，只注入 `IssueQueryApi.getAccessibleIssue` 校验问题归属；Maven enforcer 禁止它依赖 `{artifactId}-issue`。
- 领域之间只开放 `*-api` 中的 `api` 包（`XxxQueryApi` 等跨领域接口）以及 `dto.request`、`dto.response`、`enums`、`exception`；`service` 接口只给本领域 controller 和门户（portal）编排层使用。ArchUnit 以白名单校验。
- 调用第三方系统的代码统一放在 `{base}.client` 包，由 Service 调用，不散落在业务代码里。
- 两个 Service 互相调用时，把公共逻辑抽到第三个 Service 或 Repository，禁止用 `@Lazy` 绕开循环依赖。
- 模块化单体里，领域之间的 Java 依赖只能指向对方的 `*-api` 模块。跨领域编排放在 `portal`。

### 3.3 包结构

```
{base}
├── controller
├── service
│   └── impl
├── repository
│   └── impl
├── mapper
├── model
├── dto
│   ├── request
│   ├── response
│   └── bo
├── convert
├── enums
├── client
├── config
└── exception

src/main/resources/mapper/
```

模块化单体中，领域模块用 `{base}.{领域名}` 代替上面的 `{base}`，例如 `{base}.issue.controller`。`*-api` 模块只放 `service` 接口、`api` 跨领域接口、`dto.request`、`dto.response`、`enums`、`exception`。

> `domain/DTO`、`domain/VO`、`domain/BO` 为历史遗留路径，**新建文件一律不放 `domain/` 下**。新代码不再新增 VO，Mapper 查询结果统一用 BO 或 Model 承接。

<!-- MODULE_STRUCTURE_START -->
### 3.4 模块结构（模块化单体）

默认业务项目是多模块 Maven 工程，按业务领域拆，而不是按 Controller/Service/DAO 横着拆。依赖方向：

```
bootstrap ──► web ──► kernel ──► isiniu-common
     │          ▲
     ├──► portal ┘
     │       │
     │       └──► {领域}-api ──► kernel
     │
     └──► {领域}（runtime）──► {领域}-api、kernel
```

| 模块 | 放什么 | 可以依赖 | 禁止 |
|---|---|---|---|
| `{artifactId}-kernel` | 项目级约定，当前是错误码号段 | `isiniu-common` | web、portal、bootstrap、任何领域 |
| `{artifactId}-web` | 异步线程池、健康检查 | kernel | 领域模块。鉴权、跨域、全局异常、traceId、操作日志用 common 自动配置，不要再写一份 |
| `{artifactId}-{领域}-api` | 公开 Service 接口、跨领域 `api` 接口、Request、Response、枚举、ErrorCodeEnum | kernel | 实现模块、web、portal |
| `{artifactId}-{领域}` | Controller、Service 实现、Repository、Mapper、Model、BO、Convert、XML | kernel、本领域 api | web、portal、其他领域的实现模块；`isiniu-common-auth`、Spring Security（auth 除外） |
| `{artifactId}-auth` | 鉴权领域实现 | 同上，另加 `isiniu-common-auth` | 同上 |
| `{artifactId}-portal` | 面向前端的跨领域聚合 | kernel、各领域 api | 领域实现模块 |
| `{artifactId}-bootstrap` | 启动类、`application.yml`、Dockerfile、ArchUnit | web、portal、kernel；领域实现用 runtime | 在启动类里写业务 |

标杆包含三个领域：`auth`（登录与用户）、`issue`（问题）、`comment`（评论，用来演示跨领域调用）。

领域实现模块的 Maven 依赖里不要出现其他领域的实现 artifact。portal 的 enforcer 会禁止依赖 `*-issue`、`*-comment` 这类实现包。ArchUnit 再检查：controller 不依赖 repository/mapper，service 不依赖 mapper，mapper 不依赖 `dto.response`，以及一个领域的 repository/mapper/model/实现不能被另一个领域引用。

新增领域：

1. 复制 `{artifactId}-issue-api` 和 `{artifactId}-issue`，改名、改包、改类名。
2. 在父 POM 的 `<modules>` 里登记。
3. 在 `kernel` 的 `ErrorCodeRange` 登记号段。
4. 其他领域只能依赖新的 `*-api`。portal 增加对 api 的依赖，并在 enforcer 里禁止依赖新的实现模块。
5. bootstrap 增加该实现模块的 runtime 依赖。
6. Flyway 迁移脚本放到新模块的 `src/main/resources/db/migration/`，版本号按新的百号段起；技术方案写清索引，等人确认后再提交。

小工具和内部服务使用单模块模板，不套这套模块。那个模板的 `AGENTS.md` 用简化版替换本节。
<!-- MODULE_STRUCTURE_END -->

---

## 4. 命名规范

| 对象 | 规则 | 示例 |
|---|---|---|
| Controller | `XxxController` | `IssueController` |
| Service | `XxxService` / `XxxServiceImpl` | `IssueServiceImpl` |
| Repository | `XxxRepository` / `XxxRepositoryImpl` | `IssueRepository` |
| Mapper | `XxxMapper` + `XxxMapper.xml` | `IssueMapper` |
| 实体 | `XxxModel` | `IssueModel` |
| 请求 / 响应 / 业务对象 | `XxxRequest` / `XxxResponse` / `XxxBO` | `IssueCreateRequest` |
| 转换器 | `XxxConvert` | `IssueConvert` |
| 枚举 | `XxxEnum`，实现 `BaseEnum` | `IssueStatusEnum` |
| 枚举值 | 全大写下划线 | `PENDING` |
| 查询方法 | `getXxx`（单个）、`listXxx`（列表）、`pageXxx`（分页）、`countXxx`（计数） | `pageIssue` |
| Mapper 方法 | `selectXxx`、`updateXxx`、`deleteXxx` | `selectIssuePage` |
| 表名 / 字段 | 小写下划线 | `issue_main`、`create_time` |

---

## 5. 编码约定

### 5.1 注解

| 位置 | 注解 |
|---|---|
| Controller 类 | `@RestController` + `@RequestMapping` + `@Tag` |
| 接口方法 | `@Operation(summary = "...")` |
| 写操作接口 | `@OperationLog(module = "...", action = "...")` |
| POST 参数 | `@Validated @RequestBody` |
| GET 查询参数 | 直接接收对象，禁止 `@RequestBody`；需要校验时加 `@Valid` |
| Service 实现 | `@Service` + `@Slf4j` |
| 写操作方法 | `@Transactional(rollbackFor = Exception.class)` |
| 依赖注入 | `@Resource`（禁止 `@Autowired`） |
| 实体 | `@Data` + `@TableName` + `@TableId(type = IdType.AUTO)`，实现 `Serializable` |
| DTO 字段 | 全部加 `@Schema`；必填字段加 `@NotNull` / `@NotBlank` |
| 免登录接口 | 配置 `isiniu.auth.permit-paths`，不写注解 |

### 5.2 字段类型

| 场景 | 类型 | 说明 |
|---|---|---|
| ID（Request、BO、Model） | `Long` | |
| ID（Response） | `String` | 避免前端大数精度丢失 |
| 日期（Request、Model） | `LocalDateTime` | |
| 日期（Response） | `String` | 在 Convert 中用 `@Mapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")` 格式化；只有 Mapper 直接返回 BO 的统计、报表类查询，才在 SQL 里用 `DATE_FORMAT` |
| 金额 | `BigDecimal` | 数据库用 `DECIMAL`，禁止 `double/float` |

### 5.3 返回值

- Controller 统一返回 `CommonResult<T>`，用 `CommonResult.buildSuccessResult()` 包装。
- 成功码是 `0`。
- 分页返回 `CommonResult<PageResult<XxxResponse>>`，分页入参继承 `PageRequest`。
- 参数校验失败 HTTP 400，未登录 HTTP 401，其他业务异常 HTTP 200 且 body 里带业务码，未捕获异常 HTTP 500。系统异常的 body 不回传内部细节。

### 5.4 枚举

- 实现 `BaseEnum`，包含 `code`（String）、`value`（Integer）、`desc`（中文）。
- 必须提供 `getDesc(String code)` 和 `getByValue(Integer value)`。
- 状态、类型等可枚举值禁止写魔法数字，一律用枚举或常量。

### 5.5 对象转换

- 使用 MapStruct：`@Mapper(imports = {DataParseUtil.class})`，定义 `INSTANCE = Mappers.getMapper(...)`。
- Service 层赋值的字段用 `@Mapping(target = "xxx", ignore = true)`。
- 转换在 Service 层完成：Request → BO → Model；Model / BO → Response。
- `DataParseUtil` 在 `com.isiniu.common.util`。

### 5.6 其他

- 集合判空用 Spring `CollectionUtils.isEmpty()`。
- 字符串判空用 Spring `!StringUtils.hasText()`。Spring 的 `StringUtils` 没有 `isBlank`。
- 类和公共方法写中文注释，Service 接口方法写 Javadoc。

---

## 6. 异常与错误码

1. 业务异常统一抛 `CustomException`，**必须带错误码**：`throw new CustomException(ErrorCodeEnum.XXX, "补充说明")`。第三方失败时保留原始异常作为 cause。
2. 错误码格式为「模块前缀 + 4 位序号」。
   - 框架公共错误码在 `CommonErrorCode`，前缀 `SYS`，放在 isiniu-common。
   - 业务错误码在各领域的 `ErrorCodeEnum`，前缀用【项目配置】的模块前缀，序号落在 kernel `ErrorCodeRange` 分配的号段内。同一错误码全项目只有一个含义。
3. **号段分配表**（规则常量在 `com.isiniu.common.error.ErrorCodeRange`，项目内已分配清单在各项目的 kernel `ErrorCodeRange`）：

   | 段 | 前缀 | 范围 | 归属 |
   |---|---|---|---|
   | 框架通用 | `SYS` | 0001-0099 | `isiniu-common` 的 `CommonErrorCode` |
   | 鉴权 | 项目前缀 | 0100-0199 | auth 领域 |
   | 第 1 个业务领域 | 项目前缀 | 0200-0299 | 标杆为 issue |
   | 第 2 个业务领域 | 项目前缀 | 0300-0399 | 标杆为 comment |
   | 后续领域 | 项目前缀 | 每 100 个一段 | 在 kernel `ErrorCodeRange` 按顺序登记 |

   新领域必须先在 kernel `ErrorCodeRange` 登记号段，再写 `ErrorCodeEnum`；序号不得跨段重用。

4. 全局异常处理器统一转换为 `CommonResult`，Controller 内不写 `try-catch`。处理器由 isiniu-common 自动配置提供。
5. 禁止：空 `catch`、`catch` 后只打日志不处理且不说明原因、`e.printStackTrace()`、用异常做流程控制。

---

## 7. 日志与可观测性（生产可排查的底线）

1. 使用 `@Slf4j`，禁止 `System.out`。
2. **traceId**：过滤器写入 MDC，日志 pattern 使用 `%X{traceId}`；`CommonResult` 带回 `traceId`。异步线程池使用 `MdcTaskDecorator`。请求头 `X-Trace-Id` 只接受 8 到 64 位的字母、数字和连字符，避免日志注入。
3. **必须打日志的位置**：
   - 调用第三方前后：入参摘要、耗时、结果码
   - 业务状态变更：记录业务主键、变更前后状态
   - 所有 `catch` 块：带业务主键和异常堆栈
   - 定时任务、批处理：开始、结束、处理条数、失败条数
   - 接口出入口由 common 的切面统一记录，业务代码里不要再打一遍入口日志
4. **日志格式**：`[业务动作] key1={}, key2={}`，必须带业务主键：
   ```java
   log.info("[问题状态变更] issueId={}, fromStatus={}, toStatus={}", issueId, fromStatus, toStatus);
   log.error("[调用质检服务失败] taskId={}, costMs={}", taskId, cost, e);
   ```
5. **级别**：`ERROR` 需要人介入；`WARN` 可自愈或预期内的异常；`INFO` 关键业务节点；`DEBUG` 调试细节。禁止在循环里打 `INFO`。
6. **脱敏**：禁止打印密码、token、身份证号、完整手机号、银行卡号。接口切面不打印参数。

---

## 8. 数据库与事务

### 8.1 事务

1. `@Transactional(rollbackFor = Exception.class)` 只加在 Service 实现的 public 写方法上。
2. **事务内禁止调用 HTTP、RPC、发送 MQ 等远程操作**；需要保证一致性时用事务消息、本地消息表或事务提交后回调（`TransactionSynchronization`）。
3. 避免大事务：事务内不做大批量循环处理，批量操作分批提交。

### 8.2 SQL 与 MyBatis XML

1. 只用 `#{}` 参数化，**禁止** MyBatis 的 `$` 拼接。排序字段等确需动态拼接的，必须白名单校验。
2. namespace 与 Mapper 接口全路径一致；`#{}` 参数名与 `@Param` 一致。
3. 动态条件用 `<where>` + `<if>`，更新用 `<set>`，IN 条件用 `<foreach>`；字符串判空写 `param.field != null and param.field != ''`。
4. **禁止 `SELECT *`**，明确列出字段。
5. 禁止在循环里查库（N+1），改为批量查询后在内存中组装。
6. 新增查询条件必须确认有可用索引，并在技术方案中写明使用的索引。
7. 分页查询必须有排序字段，且排序字段有索引或数据量可控。
8. 通过 MyBatis Plus API 写入时，`create_time`、`update_time` 由 `MetaObjectHandler` 填充。手写 XML 的 UPDATE 不会触发填充，`update_time` 交给列上的 `ON UPDATE CURRENT_TIMESTAMP`。业务代码和 Convert 里不要给这两个字段赋值。

### 8.3 表设计

1. 每张表必须有：`id`、`create_time`、`update_time`、`deleted`（逻辑删除，实体字段加 `@TableLogic`）。
2. 逻辑删除的全局配置写在 `application.yml` 的 `mybatis-plus.global-config.db-config`。
3. 新建表、改表结构必须在技术方案中给出 DDL / ALTER 语句和索引设计，**经人确认后执行**；变更脚本统一放各模块的 `src/main/resources/db/migration/`。

### 8.4 Flyway 迁移规范

1. 脚本命名 `V{版本}__{描述}.sql`，例如 `V101__create_issue_main.sql`。
2. 版本号按领域分块：问题 1xx、鉴权 2xx、评论 3xx，新领域按顺序申请下一段百号，避免多模块合并时冲突。
3. **已经执行过的脚本禁止修改**，改表只能追加新版本脚本。
4. 表结构必须同时兼容 MySQL 8 和 H2 的 MySQL 模式：不使用 `KEY`、`ENGINE`、`CHARSET` 等 MySQL 专有内联语法；表选项和 `ON UPDATE` 用 Flyway placeholder 注入（`tableOptions`、`updateTimeOnUpdate`）。
5. 测试环境通过 Flyway 执行同一批脚本，不要再单写 `schema-h2.sql`。
6. 生产库账号需要 `CREATE`、`ALTER`、`INDEX` 权限；迁移脚本随应用启动执行，发布前先在预发库演练。

---

## 9. 接口规范

1. 新增接口必须在技术方案中写明：HTTP 方法、路径、入参、出参、权限。
2. 入参用 Bean Validation 校验，校验失败由全局异常处理器统一返回。
3. **写操作考虑幂等**：重复提交、重试不会产生重复数据（依赖业务唯一键、唯一索引或幂等 token）。并发下第二次插入撞上唯一索引时返回业务错误，调用方重试后命中已有记录。
4. 已有接口保持向后兼容：只增字段不删改字段；必须做不兼容变更时，新开接口并经人确认。
5. 查询和修改要校验数据归属。标杆问题模块只返回、只修改当前登录人的数据；越权按不存在处理，避免泄露他人数据。

---

## 10. 并发、缓存与消息

1. 线程池统一在 `config`（模块化单体放 web 模块）中定义并命名，使用 `ThreadPoolTaskExecutor`，禁止 `Executors` 快捷创建，禁止 `new Thread`。
2. 并发修改同一数据时，用乐观锁（版本号或状态条件更新，如 `UPDATE ... WHERE id = ? AND status = ?`）或分布式锁，分布式锁必须设置超时。
3. 缓存必须设置过期时间；更新采用「先更新数据库，再删除缓存」。
4. MQ 消费必须幂等，明确重试次数和失败后的处理方式。

---

## 11. 安全基线

1. 接口默认需要登录；免登录路径只能通过 `isiniu.auth.permit-paths` 配置，新增白名单必须在方案中说明。
2. 鉴权由 `isiniu-common-auth` 的 Spring Security 自动配置提供，两种模式通过 `isiniu.auth.mode` 切换：
   - `local`：内置账号密码登录，BCrypt 存储，签发 access token + refresh token；refresh token 只存哈希并轮换。
   - `sso`：作为 OAuth2 Resource Server 校验 OIDC JWT，必须配置 `issuer-uri`（校验 iss）和 `audiences`（校验 aud），可选 `jwk-set-uri` 直接指定公钥地址，通过 `sys_user.external_id` 绑定本地账号。
   业务代码只通过 `isiniu-common` 的 `LoginUserHolder` 取用户信息，不直接依赖 Spring Security 的类型。`LoginUserHolder` 基于 ThreadLocal，由 `isiniu-common-auth` 在认证完成后写入、请求结束时清理。模块化项目中，除 auth 和 bootstrap 外的模块用 Maven enforcer 禁止依赖 `isiniu-common-auth` 和 `org.springframework.security:*`。
3. 跨域由 `isiniu.web.cors-allowed-origins` 配置，默认 `*` 只用于本地开发；**每个项目上线前必须换成具体域名**。
4. **数据权限**：查询、修改数据时校验数据归属（当前用户、团队、项目），防止越权访问。
5. 禁止在代码和配置文件中硬编码密码、密钥、token，一律从配置中心或环境变量读取。数据库密码使用 `DB_PASSWORD`，JWT 密钥使用 `AUTH_JWT_SECRET`。
6. 用户输入一律视为不可信：参数校验、防 SQL 注入、文件上传校验类型和大小。

---

## 12. 自检清单（每个任务完成后必须执行）

**自动检查**

- [ ] `mvn compile` 无错误
- [ ] `mvn test` 全部通过（含 ArchUnit 分层校验，见第 13 节）

**规范检查**

- [ ] 分层调用正确：Controller → Service → Repository → Mapper，无跨层注入
- [ ] Controller 方法有 `@Operation`，写操作有 `@OperationLog`
- [ ] 写操作有 `@Transactional(rollbackFor = Exception.class)`，事务内无远程调用
- [ ] 参数校验注解齐全，DTO 字段有 `@Schema`
- [ ] import 使用 `jakarta.*`，注入使用 `@Resource`
- [ ] SQL 只用 `#{}`，无 `SELECT *`，无循环查库
- [ ] 业务异常带错误码，第三方调用和 `catch` 块有日志且带业务主键
- [ ] 新增或修改的业务逻辑有单元测试
- [ ] 无硬编码密钥，敏感信息已脱敏

**输出**

- [ ] 新建和修改的文件清单
- [ ] 每个功能点对应的验收方式（怎么测、预期结果）
- [ ] 遗留问题和需要人确认的事项

---

## 13. 质量门禁（CI 执行，不通过不能合并）

- [ ] 编译通过、单元测试通过
- [ ] ArchUnit：`controller` 不依赖 `repository`、`mapper`；`service` 不依赖 `mapper`；`mapper` 不依赖 `dto.response`；新代码不依赖 `domain` 包
- [ ] 模块化单体额外检查：领域内部类型不被其他领域依赖；Maven enforcer 禁止 portal / 其他领域编译期依赖领域实现包
- [ ] 格式检查（Spotless，Palantir Java Format）
- [ ] 依赖漏洞扫描在 CI 中执行。不放进 `mvn verify`：OWASP dependency-check 需要 NVD 密钥且不稳定。当前使用 Trivy，发现 HIGH 或 CRITICAL 就让构建失败。基线是 Spring Boot 4.1.2 开源线。有修复版本的漏洞必须升级依赖。只有确认没有开源修复、或评估后不受影响时，才在根目录 `.trivyignore` 登记（格式 `CVE-xxxx  # 原因; 到期日 YYYY-MM-DD; 负责人`，到期必须复查）。禁止通过降低 severity 或关闭扫描绕过
- [ ] 至少 1 人 Code Review

---

## 14. 文档位置

| 文档 | 路径 |
|---|---|
| 需求文档 | `docs/prd/` |
| 技术方案 | `docs/design/` |
| 架构决策记录（ADR） | `docs/adr/NNNN-标题.md` |
| 项目状态页 | `docs/STATUS.md` |
| DDL / 迁移脚本 | `src/main/resources/db/migration/` |

任务涉及设计变化时，AI 需同步更新对应的技术方案文档。

---

## 15. 禁止事项速查

| 禁止 | 正确做法 |
|---|---|
| Controller 注入 Repository / Mapper | 只注入 Service |
| Service 直接调用 Mapper | 通过 Repository |
| Mapper 返回 Response | 返回 BO / Model，Service 层转换 |
| 领域实现互相依赖 | 只依赖对方的 `*-api`，或放到 portal 聚合 |
| 新文件放 `domain/`、新增 VO | 放 `dto/request`、`dto/response`、`dto/bo` |
| Java EE 的 `javax.*` | `jakarta.*` |
| `@Autowired` | `@Resource` |
| SQL 用 `$` 拼接、`SELECT *` | `#{}`、明确列出字段 |
| 循环内查库 | 批量查询 |
| 事务内调用远程服务 | 事务提交后执行或用消息 |
| 抛异常不带错误码、空 catch | `CustomException` + 错误码 + 日志 |
| 日志不带业务主键 | `[业务动作] key={}` 格式 |
| Response 的 ID 用 `Long`、日期用 `LocalDateTime` | 用 `String`，在 Convert 中转换 |
| 金额用 `double/float` | `BigDecimal` |
| GET 请求用 `@RequestBody` | 直接接收对象 |
| 未经确认新增依赖、改表、改公共接口 | 停下来列方案，等人确认 |
| 把 isiniu-common 源码复制进业务项目 | 依赖 jar |
