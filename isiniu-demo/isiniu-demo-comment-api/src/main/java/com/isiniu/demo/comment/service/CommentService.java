package com.isiniu.demo.comment.service;

import com.isiniu.demo.comment.dto.request.CommentCreateRequest;
import com.isiniu.demo.comment.dto.response.CommentResponse;
import java.util.List;

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
     * 查询某个问题下的评论。同样先校验问题归属。
     *
     * @param issueId 问题 ID
     * @return 评论列表
     */
    List<CommentResponse> listByIssue(Long issueId);
}
