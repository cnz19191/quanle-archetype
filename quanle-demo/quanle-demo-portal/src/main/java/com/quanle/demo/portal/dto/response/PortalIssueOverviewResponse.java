package com.quanle.demo.portal.dto.response;

import com.quanle.common.api.PageResult;
import com.quanle.demo.issue.dto.response.IssueResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门户上的问题总览。演示 portal 组合登录上下文和领域服务。
 *
 * @author quanle
 */
@Data
@Schema(description = "门户问题总览")
public class PortalIssueOverviewResponse {

    @Schema(description = "当前用户 ID")
    private String userId;

    @Schema(description = "问题分页")
    private PageResult<IssueResponse> page;
}
