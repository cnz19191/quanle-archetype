package com.isiniu.demo.comment.repository;

import com.isiniu.common.mybatis.MpBaseService;
import com.isiniu.demo.comment.model.CommentModel;
import java.util.List;

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
     * 查询问题下的评论，按创建时间倒序。
     *
     * @param issueId 问题 ID
     * @return 评论列表
     */
    List<CommentModel> listByIssueId(Long issueId);
}
