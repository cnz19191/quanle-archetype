package com.isiniu.demo.issue.controller;

import com.isiniu.common.api.CommonResult;
import com.isiniu.common.api.PageResult;
import com.isiniu.common.web.OperationLog;
import com.isiniu.demo.issue.dto.request.IssueCreateRequest;
import com.isiniu.demo.issue.dto.request.IssuePageRequest;
import com.isiniu.demo.issue.dto.request.IssueStatusChangeRequest;
import com.isiniu.demo.issue.dto.response.IssueResponse;
import com.isiniu.demo.issue.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问题管理接口。
 *
 * @author isiniu
 */
@Validated
@RestController
@RequestMapping("/api/issues")
@Tag(name = "问题管理")
public class IssueController {

    @Resource
    private IssueService issueService;

    @GetMapping
    @Operation(summary = "分页查询问题")
    public CommonResult<PageResult<IssueResponse>> pageIssue(@Valid IssuePageRequest request) {
        return CommonResult.buildSuccessResult(issueService.pageIssue(request));
    }

    @PostMapping
    @Operation(summary = "新建问题")
    @OperationLog(module = "问题管理", action = "新建问题")
    public CommonResult<IssueResponse> createIssue(@Validated @RequestBody IssueCreateRequest request) {
        return CommonResult.buildSuccessResult(issueService.createIssue(request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "变更问题状态")
    @OperationLog(module = "问题管理", action = "变更状态")
    public CommonResult<IssueResponse> changeStatus(
            @PathVariable("id") Long id, @Validated @RequestBody IssueStatusChangeRequest request) {
        return CommonResult.buildSuccessResult(issueService.changeStatus(id, request));
    }
}
