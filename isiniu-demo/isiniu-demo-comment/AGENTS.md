# isiniu-demo-comment

评论领域实现，演示跨领域调用的正确做法。

- 依赖 `isiniu-demo-comment-api` 和 `isiniu-demo-issue-api`
- 通过 `IssueService`（issue-api）校验问题归属，**不依赖** `isiniu-demo-issue` 实现
- 不依赖 web、portal、bootstrap 和其他领域实现
- 评论只存 `issue_id`，不复制问题字段

完整规约见仓库根目录 `AGENTS.md`。
