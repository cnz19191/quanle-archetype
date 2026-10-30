package com.isiniu.demo.comment.archfixture;

import com.isiniu.demo.issue.api.IssueQueryApi;
import com.isiniu.demo.issue.dto.response.IssueBriefResponse;
import com.isiniu.demo.issue.enums.IssueStatusEnum;

/**
 * ArchUnit 正例：评论领域只使用问题领域公开的 api、dto.response、enums，白名单规则必须放行。
 *
 * @author isiniu
 */
public class CommentUsesIssuePublicApi {

    private IssueQueryApi issueQueryApi;

    public boolean isPending(Long id) {
        IssueBriefResponse issue = issueQueryApi.getAccessibleIssue(id);
        return IssueStatusEnum.PENDING.name().equals(issue.getStatus());
    }
}
