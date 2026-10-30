package com.isiniu.demo.comment.service;

import com.isiniu.common.api.PageRequest;
import com.isiniu.common.api.PageResult;
import com.isiniu.demo.comment.dto.request.CommentCreateRequest;
import com.isiniu.demo.comment.dto.response.CommentResponse;

/**
 * 评论领域对外服务。实现模块通过 issue-api 校验问题归属，演示合规的跨领域调用。
 *
 * @author isiniu
 */
public interface CommentService {

    /**
     * 新建评论。先调用问题领域校验问题存在且属于当前用户。
     *
     * @param issueId 问题 ID
     * @param request 新建参数
     * @return 评论
     */
    CommentResponse createComment(Long issueId, CommentCreateRequest request);

    /**
     * 分页查询某个问题下的评论，按创建时间倒序。同样先校验问题归属。
     *
     * @param issueId 问题 ID
     * @param request 分页参数，每页最多 100 条
     * @return 评论分页结果
     */
    PageResult<CommentResponse> pageByIssue(Long issueId, PageRequest request);
}
