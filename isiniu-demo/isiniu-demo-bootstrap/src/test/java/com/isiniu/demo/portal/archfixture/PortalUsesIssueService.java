package com.isiniu.demo.portal.archfixture;

import com.isiniu.demo.issue.service.IssueService;

/**
 * ArchUnit 正例：门户编排层可以调用领域的 service 接口。
 *
 * @author isiniu
 */
public class PortalUsesIssueService {

    private IssueService issueService;

    public void touch(Long id) {
        issueService.getIssue(id);
    }
}
