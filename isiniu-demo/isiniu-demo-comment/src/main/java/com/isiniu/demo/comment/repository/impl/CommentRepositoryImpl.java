package com.isiniu.demo.comment.repository.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isiniu.common.mybatis.MpBaseServiceImpl;
import com.isiniu.demo.comment.mapper.CommentMapper;
import com.isiniu.demo.comment.model.CommentModel;
import com.isiniu.demo.comment.repository.CommentRepository;
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
    public Page<CommentModel> pageByIssueId(Long issueId, long pageNum, long pageSize) {
        return lambdaQuery()
                .eq(CommentModel::getIssueId, issueId)
                .orderByDesc(CommentModel::getCreateTime)
                .orderByDesc(CommentModel::getId)
                .page(new Page<>(pageNum, pageSize));
    }
}
