# 0001 按领域拆模块化单体

## 状态

已接受

## 背景

团队约 20 人，用 Codex、Claude Code、Cursor 写 Java 后端。按技术层横拆（api/biz/web）会让一个业务改动穿过很多模块；所有项目又拆成微服务则过重。

## 决定

- 主力模板是模块化单体：kernel、web、领域 api、领域实现、portal、bootstrap。
- 领域实现之间不能互相依赖。跨领域只依赖对方的 api 模块，或者在 portal 聚合。
- 小工具使用单模块模板，分层规则相同。
- 两个模板都依赖 `isiniu-common` jar。

## 后果

新增领域要同时改父 POM、bootstrap 的 runtime 依赖和 portal 的 enforcer。换来的是编译期就看不了别的领域的 Mapper 和 Model。
