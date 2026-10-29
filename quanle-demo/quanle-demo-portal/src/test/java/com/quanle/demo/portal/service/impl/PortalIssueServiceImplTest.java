package com.quanle.demo.portal.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quanle.common.api.PageResult;
import com.quanle.common.exception.CustomException;
import com.quanle.common.web.CurrentUserHolder;
import com.quanle.demo.issue.dto.request.IssuePageRequest;
import com.quanle.demo.issue.dto.response.IssueResponse;
import com.quanle.demo.issue.service.IssueService;
import com.quanle.demo.portal.dto.response.PortalIssueOverviewResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 门户只聚合领域服务。
 *
 * @author quanle
 */
@ExtendWith(MockitoExtension.class)
class PortalIssueServiceImplTest {

    @Mock
    private IssueService issueService;

    @InjectMocks
    private PortalIssueServiceImpl portalIssueService;

    @AfterEach
    void tearDown() {
        CurrentUserHolder.clear();
    }

    @Test
    void overviewUsesCurrentUserAndIssueService() {
        CurrentUserHolder.setUserId(7L);
        IssuePageRequest request = new IssuePageRequest();
        PageResult<IssueResponse> page = PageResult.of(0, 1, 10, List.of());
        when(issueService.pageIssue(request)).thenReturn(page);

        PortalIssueOverviewResponse response = portalIssueService.overview(request);

        assertEquals("7", response.getUserId());
        assertEquals(page, response.getPage());
        verify(issueService).pageIssue(request);
    }

    @Test
    void overviewRejectsAnonymous() {
        IssuePageRequest request = new IssuePageRequest();
        assertThrows(CustomException.class, () -> portalIssueService.overview(request));
    }
}
