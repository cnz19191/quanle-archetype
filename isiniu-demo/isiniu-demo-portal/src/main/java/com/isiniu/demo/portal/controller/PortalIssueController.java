package com.isiniu.demo.portal.controller;

import com.isiniu.common.api.CommonResult;
import com.isiniu.demo.issue.dto.request.IssuePageRequest;
import com.isiniu.demo.portal.dto.response.PortalIssueOverviewResponse;
import com.isiniu.demo.portal.service.PortalIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门户问题总览接口。
 *
 * @author isiniu
 */
@Validated
@RestController
@RequestMapping("/api/portal/issues")
@Tag(name = "门户-问题")
public class PortalIssueController {

    @Resource
    private PortalIssueService portalIssueService;

    @GetMapping
    @Operation(summary = "当前用户的问题总览")
    public CommonResult<PortalIssueOverviewResponse> overview(@Valid IssuePageRequest request) {
        return CommonResult.buildSuccessResult(portalIssueService.overview(request));
    }
}
