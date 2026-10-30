package com.isiniu.single.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.isiniu.common.mybatis.MpBaseService;
import com.isiniu.single.dto.bo.IssueBO;
import com.isiniu.single.dto.bo.IssuePageQueryBO;
import com.isiniu.single.model.IssueModel;

/**
 * 问题数据访问。Service 只依赖本接口。
 *
 * @author isiniu
 */
public interface IssueRepository extends MpBaseService<IssueModel> {

    /**
     * 按业务幂等号查询未删除的问题，不按提交人过滤，供幂等判断使用。
     *
     * @param bizNo 业务幂等号
     * @return 问题，不存在时返回 null
     */
    IssueModel getByBizNo(String bizNo);

    /**
     * 分页查询问题。调用方必须带上 reporterId。排序字段为 update_time、id。
     *
     * @param query 查询条件
     * @return 分页数据
     */
    Page<IssueBO> pageIssue(IssuePageQueryBO query);

    /**
     * 按期望状态条件更新。影响行数为 0 表示状态已被别人改掉。
     *
     * @param id 问题 ID
     * @param expectStatus 期望的当前状态
     * @param targetStatus 目标状态
     * @param reporterId 提交人
     * @return 影响行数
     */
    int updateStatus(Long id, Integer expectStatus, Integer targetStatus, Long reporterId);
}
