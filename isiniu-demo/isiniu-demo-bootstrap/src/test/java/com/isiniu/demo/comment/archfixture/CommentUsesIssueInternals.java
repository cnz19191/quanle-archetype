package com.isiniu.demo.comment.archfixture;

import com.isiniu.demo.issue.model.IssueModel;
import com.isiniu.demo.issue.repository.IssueRepository;
import com.isiniu.demo.issue.service.IssueService;

/**
 * ArchUnit 反例：评论领域直接使用问题领域的 Repository、Model 和 Service 接口，白名单规则必须报错。
 *
 * @author isiniu
 */
public class CommentUsesIssueInternals {

    private IssueRepository issueRepository;

    private IssueService issueService;

    public IssueModel load(Long id) {
        issueService.getIssue(id);
        return issueRepository.getById(id);
    }
}
