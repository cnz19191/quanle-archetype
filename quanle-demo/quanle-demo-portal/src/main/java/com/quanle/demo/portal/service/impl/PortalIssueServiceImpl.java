package com.quanle.demo.portal.service.impl;

import com.quanle.common.enums.CommonErrorCode;
import com.quanle.common.exception.CustomException;
import com.quanle.common.util.DataParseUtil;
import com.quanle.common.web.CurrentUserHolder;
import com.quanle.demo.issue.dto.request.IssuePageRequest;
import com.quanle.demo.issue.service.IssueService;
import com.quanle.demo.portal.dto.response.PortalIssueOverviewResponse;
import com.quanle.demo.portal.service.PortalIssueService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 门户问题总览实现。只调用领域 Service 接口。
 *
 * @author quanle
 */
@Slf4j
@Service
public class PortalIssueServiceImpl implements PortalIssueService {

    @Resource
    private IssueService issueService;

    @Override
    public PortalIssueOverviewResponse overview(IssuePageRequest request) {
        Long userId = CurrentUserHolder.getUserId();
        if (userId == null) {
            log.warn("[门户问题总览失败] reason=unauthorized");
            throw new CustomException(CommonErrorCode.UNAUTHORIZED, "未登录");
        }
        log.info("[门户问题总览] userId={}, pageNum={}", userId, request.getPageNum());
        PortalIssueOverviewResponse response = new PortalIssueOverviewResponse();
        response.setUserId(DataParseUtil.toIdString(userId));
        response.setPage(issueService.pageIssue(request));
        return response;
    }
}
