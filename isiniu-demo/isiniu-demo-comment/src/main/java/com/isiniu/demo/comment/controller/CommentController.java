package com.isiniu.demo.comment.controller;

import com.isiniu.common.api.CommonResult;
import com.isiniu.common.web.OperationLog;
import com.isiniu.demo.comment.dto.request.CommentCreateRequest;
import com.isiniu.demo.comment.dto.response.CommentResponse;
import com.isiniu.demo.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论接口。创建前由领域服务校验问题归属。
 *
 * @author isiniu
 */
@Validated
@RestController
@RequestMapping("/api/issues/{issueId}/comments")
@Tag(name = "评论管理")
public class CommentController {

    @Resource
    private CommentService commentService;

    @GetMapping
    @Operation(summary = "查询问题下的评论")
    public CommonResult<List<CommentResponse>> list(@PathVariable("issueId") Long issueId) {
        return CommonResult.buildSuccessResult(commentService.listByIssue(issueId));
    }

    @PostMapping
    @Operation(summary = "新建评论")
    @OperationLog(module = "评论管理", action = "新建评论")
    public CommonResult<CommentResponse> create(
            @PathVariable("issueId") Long issueId, @Validated @RequestBody CommentCreateRequest request) {
        return CommonResult.buildSuccessResult(commentService.createComment(issueId, request));
    }
}
