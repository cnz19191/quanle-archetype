package com.isiniu.demo.issue.service.impl;

import com.isiniu.demo.issue.api.IssueQueryApi;
import com.isiniu.demo.issue.convert.IssueConvert;
import com.isiniu.demo.issue.dto.response.IssueBriefResponse;
import com.isiniu.demo.issue.service.IssueService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 问题只读查询接口实现。归属校验复用 {@link IssueService#getIssue(Long)}。
 *
 * @author isiniu
 */
@Service
public class IssueQueryApiImpl implements IssueQueryApi {

    @Resource
    private IssueService issueService;

    @Override
    public IssueBriefResponse getAccessibleIssue(Long id) {
        return IssueConvert.INSTANCE.toBriefResponse(issueService.getIssue(id));
    }
}
