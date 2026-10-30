package com.isiniu.demo.comment.repository.impl;

import com.isiniu.common.mybatis.MpBaseServiceImpl;
import com.isiniu.demo.comment.mapper.CommentMapper;
import com.isiniu.demo.comment.model.CommentModel;
import com.isiniu.demo.comment.repository.CommentRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 评论数据访问实现。
 *
 * @author isiniu
 */
@Repository
public class CommentRepositoryImpl extends MpBaseServiceImpl<CommentMapper, CommentModel> implements CommentRepository {

    @Override
    public CommentModel getByBizNo(Long issueId, String bizNo) {
        if (issueId == null || !StringUtils.hasText(bizNo)) {
            return null;
        }
        return lambdaQuery()
                .eq(CommentModel::getIssueId, issueId)
                .eq(CommentModel::getBizNo, bizNo)
                .one();
    }

    @Override
    public List<CommentModel> listByIssueId(Long issueId) {
        return lambdaQuery()
                .eq(CommentModel::getIssueId, issueId)
                .orderByDesc(CommentModel::getCreateTime)
                .orderByDesc(CommentModel::getId)
                .list();
    }
}
