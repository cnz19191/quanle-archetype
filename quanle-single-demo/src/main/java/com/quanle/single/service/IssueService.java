package com.quanle.single.service;

import com.quanle.common.api.PageResult;
import com.quanle.single.dto.request.IssueCreateRequest;
import com.quanle.single.dto.request.IssuePageRequest;
import com.quanle.single.dto.request.IssueStatusChangeRequest;
import com.quanle.single.dto.response.IssueResponse;

/**
 * 问题领域对外服务。其他领域只能依赖本接口，不能依赖实现模块里的 Repository 或 Mapper。
 *
 * @author quanle
 */
public interface IssueService {

    /**
     * 新建问题。以 bizNo 做幂等：同一提交人、同一标题的重复请求返回已有记录。
     *
     * @param request 新建参数
     * @return 问题
     */
    IssueResponse createIssue(IssueCreateRequest request);

    /**
     * 分页查询当前登录人的问题。
     *
     * @param request 分页和筛选条件
     * @return 分页结果
     */
    PageResult<IssueResponse> pageIssue(IssuePageRequest request);

    /**
     * 按状态机变更问题状态。更新语句带当前状态条件，避免并发覆盖。
     *
     * @param id 问题 ID
     * @param request 目标状态
     * @return 变更后的问题
     */
    IssueResponse changeStatus(Long id, IssueStatusChangeRequest request);
}
