package com.quanle.single.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanle.common.api.PageResult;
import com.quanle.common.enums.CommonErrorCode;
import com.quanle.common.exception.CustomException;
import com.quanle.common.web.CurrentUserHolder;
import com.quanle.single.dto.bo.IssueBO;
import com.quanle.single.dto.bo.IssuePageQueryBO;
import com.quanle.single.dto.request.IssueCreateRequest;
import com.quanle.single.dto.request.IssuePageRequest;
import com.quanle.single.dto.request.IssueStatusChangeRequest;
import com.quanle.single.dto.response.IssueResponse;
import com.quanle.single.enums.IssueStatusEnum;
import com.quanle.single.exception.ErrorCodeEnum;
import com.quanle.single.model.IssueModel;
import com.quanle.single.repository.IssueRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

/**
 * 问题服务的幂等、数据权限和状态条件更新。
 *
 * @author quanle
 */
@ExtendWith(MockitoExtension.class)
class IssueServiceImplTest {

    private static final LocalDateTime TIME = LocalDateTime.of(2026, 1, 2, 3, 4, 5);

    @Mock
    private IssueRepository issueRepository;

    @InjectMocks
    private IssueServiceImpl issueService;

    @BeforeEach
    void setUp() {
        CurrentUserHolder.setUserId(7L);
    }

    @AfterEach
    void tearDown() {
        CurrentUserHolder.clear();
    }

    @Test
    void createIssueAssignsCurrentUserAndPendingStatus() {
        when(issueRepository.getByBizNo("BIZ-1")).thenReturn(null);
        when(issueRepository.save(any(IssueModel.class))).thenAnswer(invocation -> {
            IssueModel model = invocation.getArgument(0);
            model.setId(10L);
            model.setCreateTime(TIME);
            model.setUpdateTime(TIME);
            return true;
        });

        IssueResponse response = issueService.createIssue(request("打印机无法连接", "BIZ-1"));

        ArgumentCaptor<IssueModel> captor = ArgumentCaptor.forClass(IssueModel.class);
        verify(issueRepository).save(captor.capture());
        assertEquals(7L, captor.getValue().getReporterId());
        assertEquals(IssueStatusEnum.PENDING.getValue(), captor.getValue().getStatus());
        assertEquals("10", response.getId());
        assertEquals("7", response.getReporterId());
        assertEquals("PENDING", response.getStatus());
        assertEquals("待处理", response.getStatusDesc());
        assertEquals("2026-01-02 03:04:05", response.getCreateTime());
    }

    @Test
    void createIssueReturnsExistingWhenIdempotent() {
        when(issueRepository.getByBizNo("BIZ-1")).thenReturn(existing(3L, 7L, "打印机无法连接"));

        IssueResponse response = issueService.createIssue(request("  打印机无法连接  ", "BIZ-1"));

        assertEquals("3", response.getId());
        verify(issueRepository, never()).save(any());
    }

    @Test
    void createIssueRejectsDifferentTitle() {
        when(issueRepository.getByBizNo("BIZ-1")).thenReturn(existing(3L, 7L, "打印机无法连接"));
        CustomException ex =
                assertThrows(CustomException.class, () -> issueService.createIssue(request("别的标题", "BIZ-1")));
        assertEquals(ErrorCodeEnum.ISSUE_DUPLICATE.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void createIssueDoesNotExposeAnotherUsersIssue() {
        when(issueRepository.getByBizNo("BIZ-1")).thenReturn(existing(3L, 8L, "打印机无法连接"));
        CustomException ex =
                assertThrows(CustomException.class, () -> issueService.createIssue(request("打印机无法连接", "BIZ-1")));
        assertEquals(ErrorCodeEnum.ISSUE_DUPLICATE.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void createIssueWrapsDuplicateKey() {
        when(issueRepository.getByBizNo("BIZ-1")).thenReturn(null);
        DuplicateKeyException cause = new DuplicateKeyException("dup");
        when(issueRepository.save(any(IssueModel.class))).thenThrow(cause);
        CustomException ex =
                assertThrows(CustomException.class, () -> issueService.createIssue(request("打印机无法连接", "BIZ-1")));
        assertEquals(cause, ex.getCause());
    }

    @Test
    void pageIssueLimitsToCurrentUser() {
        Page<IssueBO> page = new Page<>(1, 10);
        IssueBO bo = new IssueBO();
        bo.setId(5L);
        bo.setTitle("打印机无法连接");
        bo.setStatus(IssueStatusEnum.PENDING.getValue());
        bo.setReporterId(7L);
        bo.setBizNo("BIZ-1");
        bo.setCreateTime(TIME);
        bo.setUpdateTime(TIME);
        page.setRecords(java.util.List.of(bo));
        page.setTotal(1);
        when(issueRepository.pageIssue(any(IssuePageQueryBO.class))).thenReturn(page);

        IssuePageRequest request = new IssuePageRequest();
        request.setTitle("打印");
        request.setStatus("PENDING");
        PageResult<IssueResponse> result = issueService.pageIssue(request);

        ArgumentCaptor<IssuePageQueryBO> captor = ArgumentCaptor.forClass(IssuePageQueryBO.class);
        verify(issueRepository).pageIssue(captor.capture());
        assertEquals(7L, captor.getValue().getReporterId());
        assertEquals(IssueStatusEnum.PENDING.getValue(), captor.getValue().getStatus());
        assertEquals("5", result.getList().get(0).getId());
        assertEquals("2026-01-02 03:04:05", result.getList().get(0).getUpdateTime());
    }

    @Test
    void changeStatusUpdatesWhenTransitionIsLegal() {
        when(issueRepository.getById(5L)).thenReturn(existing(5L, 7L, "打印机无法连接"));
        when(issueRepository.updateStatus(5L, 0, 1, 7L)).thenReturn(1);
        IssueStatusChangeRequest request = new IssueStatusChangeRequest();
        request.setTargetStatus("PROCESSING");

        IssueResponse response = issueService.changeStatus(5L, request);

        assertEquals("PROCESSING", response.getStatus());
        assertEquals("处理中", response.getStatusDesc());
    }

    @Test
    void changeStatusRejectsIllegalTransition() {
        when(issueRepository.getById(5L)).thenReturn(existing(5L, 7L, "打印机无法连接"));
        IssueStatusChangeRequest request = new IssueStatusChangeRequest();
        request.setTargetStatus("CLOSED");
        CustomException ex = assertThrows(CustomException.class, () -> issueService.changeStatus(5L, request));
        assertEquals(
                ErrorCodeEnum.ISSUE_STATUS_ILLEGAL.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void changeStatusConflictsWhenNoRowUpdated() {
        when(issueRepository.getById(5L)).thenReturn(existing(5L, 7L, "打印机无法连接"));
        when(issueRepository.updateStatus(5L, 0, 1, 7L)).thenReturn(0);
        IssueStatusChangeRequest request = new IssueStatusChangeRequest();
        request.setTargetStatus("PROCESSING");
        CustomException ex = assertThrows(CustomException.class, () -> issueService.changeStatus(5L, request));
        assertEquals(
                ErrorCodeEnum.ISSUE_STATUS_CONFLICT.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void changeStatusHidesOthersIssues() {
        when(issueRepository.getById(5L)).thenReturn(existing(5L, 8L, "打印机无法连接"));
        IssueStatusChangeRequest request = new IssueStatusChangeRequest();
        request.setTargetStatus("PROCESSING");
        CustomException ex = assertThrows(CustomException.class, () -> issueService.changeStatus(5L, request));
        assertEquals(ErrorCodeEnum.ISSUE_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void rejectsAnonymousCall() {
        CurrentUserHolder.clear();
        CustomException ex =
                assertThrows(CustomException.class, () -> issueService.createIssue(request("打印机无法连接", "BIZ-1")));
        assertEquals(CommonErrorCode.UNAUTHORIZED.getCode(), ex.getErrorCode().getCode());
    }

    private IssueCreateRequest request(String title, String bizNo) {
        IssueCreateRequest request = new IssueCreateRequest();
        request.setTitle(title);
        request.setContent("一楼");
        request.setBizNo(bizNo);
        return request;
    }

    private IssueModel existing(Long id, Long reporterId, String title) {
        IssueModel model = new IssueModel();
        model.setId(id);
        model.setTitle(title);
        model.setContent("一楼");
        model.setStatus(IssueStatusEnum.PENDING.getValue());
        model.setReporterId(reporterId);
        model.setBizNo("BIZ-1");
        model.setCreateTime(TIME);
        model.setUpdateTime(TIME);
        return model;
    }
}
