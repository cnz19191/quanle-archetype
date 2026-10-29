package com.quanle.demo.portal.service;

import com.quanle.demo.issue.dto.request.IssuePageRequest;
import com.quanle.demo.portal.dto.response.PortalIssueOverviewResponse;

/**
 * 门户问题总览。
 *
 * @author quanle
 */
public interface PortalIssueService {

    /**
     * 查询当前用户的问题总览。
     *
     * @param request 分页条件
     * @return 总览
     */
    PortalIssueOverviewResponse overview(IssuePageRequest request);
}
