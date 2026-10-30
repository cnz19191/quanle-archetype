package com.isiniu.demo.comment.service.impl;

import com.isiniu.common.enums.CommonErrorCode;
import com.isiniu.common.exception.CustomException;
import com.isiniu.common.web.LoginUserHolder;
import com.isiniu.demo.comment.convert.CommentConvert;
import com.isiniu.demo.comment.dto.request.CommentCreateRequest;
import com.isiniu.demo.comment.dto.response.CommentResponse;
import com.isiniu.demo.comment.exception.ErrorCodeEnum;
import com.isiniu.demo.comment.model.CommentModel;
import com.isiniu.demo.comment.repository.CommentRepository;
import com.isiniu.demo.comment.service.CommentService;
import com.isiniu.demo.issue.service.IssueService;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评论领域服务实现。通过 issue-api 校验问题归属，不依赖问题领域的实现。
 *
 * @author isiniu
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentRepository commentRepository;

    /** 跨领域调用：只依赖 issue-api 暴露的 Service 接口。 */
    @Resource
    private IssueService issueService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentResponse createComment(Long issueId, CommentCreateRequest request) {
        Long authorId = currentUserId();
        issueService.getIssue(issueId);
        CommentModel existing = commentRepository.getByBizNo(issueId, request.getBizNo());
        if (existing != null) {
            return reuseOrReject(request, existing, authorId);
        }
        CommentModel model = CommentConvert.INSTANCE.toModel(request);
        model.setIssueId(issueId);
        model.setAuthorId(authorId);
        try {
            commentRepository.save(model);
        } catch (DuplicateKeyException ex) {
            log.warn("[创建评论唯一键冲突] issueId={}, bizNo={}, authorId={}", issueId, request.getBizNo(), authorId, ex);
            throw new CustomException(ErrorCodeEnum.COMMENT_DUPLICATE, "bizNo=" + request.getBizNo(), ex);
        }
        log.info("[创建评论] issueId={}, commentId={}, authorId={}", issueId, model.getId(), authorId);
        return CommentConvert.INSTANCE.toResponse(model);
    }

    @Override
    public List<CommentResponse> listByIssue(Long issueId) {
        issueService.getIssue(issueId);
        return commentRepository.listByIssueId(issueId).stream()
                .map(CommentConvert.INSTANCE::toResponse)
                .toList();
    }

    private CommentResponse reuseOrReject(CommentCreateRequest request, CommentModel existing, Long authorId) {
        if (!authorId.equals(existing.getAuthorId()) || !existing.getContent().equals(request.getContent())) {
            log.warn(
                    "[创建评论幂等冲突] issueId={}, commentId={}, authorId={}",
                    existing.getIssueId(),
                    existing.getId(),
                    authorId);
            throw new CustomException(ErrorCodeEnum.COMMENT_DUPLICATE, "bizNo=" + request.getBizNo());
        }
        return CommentConvert.INSTANCE.toResponse(existing);
    }

    private Long currentUserId() {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED, "未登录");
        }
        return userId;
    }
}
