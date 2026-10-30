package com.isiniu.single.repository.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isiniu.common.mybatis.MpBaseServiceImpl;
import com.isiniu.single.dto.bo.IssueBO;
import com.isiniu.single.dto.bo.IssuePageQueryBO;
import com.isiniu.single.mapper.IssueMapper;
import com.isiniu.single.model.IssueModel;
import com.isiniu.single.repository.IssueRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 问题数据访问实现。
 *
 * @author isiniu
 */
@Repository
public class IssueRepositoryImpl extends MpBaseServiceImpl<IssueMapper, IssueModel> implements IssueRepository {

    @Override
    public IssueModel getByBizNo(String bizNo) {
        if (!StringUtils.hasText(bizNo)) {
            return null;
        }
        return lambdaQuery().eq(IssueModel::getBizNo, bizNo).one();
    }

    @Override
    public Page<IssueBO> pageIssue(IssuePageQueryBO query) {
        long current = query.getPageNum() == null ? 1L : query.getPageNum();
        long size = query.getPageSize() == null ? 10L : query.getPageSize();
        Page<IssueBO> page = new Page<>(current, size);
        Page<IssueBO> result = getBaseMapper().selectIssuePage(page, query);
        if (result == null) {
            return page;
        }
        return result;
    }

    @Override
    public int updateStatus(Long id, Integer expectStatus, Integer targetStatus, Long reporterId) {
        return getBaseMapper().updateIssueStatus(id, expectStatus, targetStatus, reporterId);
    }
}
