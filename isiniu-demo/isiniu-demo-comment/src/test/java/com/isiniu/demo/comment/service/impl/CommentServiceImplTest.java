package com.isiniu.demo.comment.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.isiniu.common.exception.CustomException;
import com.isiniu.common.web.LoginUserHolder;
import com.isiniu.demo.comment.dto.request.CommentCreateRequest;
import com.isiniu.demo.comment.exception.ErrorCodeEnum;
import com.isiniu.demo.comment.model.CommentModel;
import com.isiniu.demo.comment.repository.CommentRepository;
import com.isiniu.demo.issue.api.IssueQueryApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 评论服务：通过 issue-api 校验归属，不依赖问题实现。
 *
 * @author isiniu
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private IssueQueryApi issueQueryApi;

    @InjectMocks
    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        LoginUserHolder.setUserId(7L);
    }

    @AfterEach
    void tearDown() {
        LoginUserHolder.clear();
    }

    @Test
    void createChecksIssueThroughApi() {
        when(commentRepository.getByBizNo(1L, "CMT-1")).thenReturn(null);

        commentService.createComment(1L, request("评论内容", "CMT-1"));

        verify(issueQueryApi).getAccessibleIssue(1L);
        verify(commentRepository).save(any(CommentModel.class));
    }

    @Test
    void duplicateWithDifferentContentIsRejected() {
        CommentModel existing = new CommentModel();
        existing.setId(9L);
        existing.setIssueId(1L);
        existing.setAuthorId(7L);
        existing.setContent("原内容");
        when(commentRepository.getByBizNo(1L, "CMT-1")).thenReturn(existing);

        CustomException ex =
                assertThrows(CustomException.class, () -> commentService.createComment(1L, request("新内容", "CMT-1")));

        assertEquals(
                ErrorCodeEnum.COMMENT_DUPLICATE.getCode(), ex.getErrorCode().getCode());
    }

    private CommentCreateRequest request(String content, String bizNo) {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setContent(content);
        request.setBizNo(bizNo);
        return request;
    }
}
