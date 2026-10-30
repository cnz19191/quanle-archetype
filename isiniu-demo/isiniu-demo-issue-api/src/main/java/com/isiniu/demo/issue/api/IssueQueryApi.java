package com.isiniu.demo.issue.api;

import com.isiniu.demo.issue.dto.response.IssueBriefResponse;

/**
 * 问题领域对其他领域开放的只读查询接口。其他领域只注入本接口，不注入 {@code IssueService}。
 *
 * @author isiniu
 */
public interface IssueQueryApi {

    /**
     * 查询当前登录人可以访问的问题摘要，用于其他领域校验问题归属。
     * 问题不存在或不属于当前登录人时抛出 {@code ISSUE_NOT_FOUND}。
     *
     * @param id 问题 ID
     * @return 问题摘要
     */
    IssueBriefResponse getAccessibleIssue(Long id);
}
