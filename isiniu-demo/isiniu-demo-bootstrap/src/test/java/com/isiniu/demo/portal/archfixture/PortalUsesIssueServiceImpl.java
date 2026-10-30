package com.isiniu.demo.portal.archfixture;

import com.isiniu.demo.issue.service.impl.IssueServiceImpl;

/**
 * ArchUnit 反例：门户也不能依赖领域的 service 实现。
 *
 * @author isiniu
 */
public class PortalUsesIssueServiceImpl {

    private IssueServiceImpl issueService;

    public void touch(Long id) {
        issueService.getIssue(id);
    }
}
