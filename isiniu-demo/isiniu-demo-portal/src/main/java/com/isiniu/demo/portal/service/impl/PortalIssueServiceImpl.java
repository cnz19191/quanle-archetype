package com.isiniu.demo.portal.service.impl;

import com.isiniu.common.enums.CommonErrorCode;
import com.isiniu.common.exception.CustomException;
import com.isiniu.common.util.DataParseUtil;
import com.isiniu.common.web.LoginUserHolder;
import com.isiniu.demo.issue.dto.request.IssuePageRequest;
import com.isiniu.demo.issue.service.IssueService;
import com.isiniu.demo.portal.dto.response.PortalIssueOverviewResponse;
import com.isiniu.demo.portal.service.PortalIssueService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 门户问题总览实现。只调用领域 Service 接口。
 *
 * @author isiniu
 */
@Slf4j
@Service
public class PortalIssueServiceImpl implements PortalIssueService {

    @Resource
    private IssueService issueService;

    @Override
    public PortalIssueOverviewResponse overview(IssuePageRequest request) {
        Long userId = LoginUserHolder.getUserId();
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
