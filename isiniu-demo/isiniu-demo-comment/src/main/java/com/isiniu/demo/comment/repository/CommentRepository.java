package com.isiniu.demo.comment.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isiniu.common.mybatis.MpBaseService;
import com.isiniu.demo.comment.model.CommentModel;

/**
 * 评论数据访问。
 *
 * @author isiniu
 */
public interface CommentRepository extends MpBaseService<CommentModel> {

    /**
     * 按问题和业务幂等号查询未删除的评论。
     *
     * @param issueId 问题 ID
     * @param bizNo 业务幂等号
     * @return 评论，不存在时返回 null
     */
    CommentModel getByBizNo(Long issueId, String bizNo);

    /**
     * 分页查询问题下的评论，按创建时间倒序。
     *
     * @param issueId 问题 ID
     * @param pageNum 页码，从 1 开始
     * @param pageSize 每页条数
     * @return 评论分页
     */
    Page<CommentModel> pageByIssueId(Long issueId, long pageNum, long pageSize);
}
