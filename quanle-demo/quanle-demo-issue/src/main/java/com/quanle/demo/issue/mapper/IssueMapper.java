package com.quanle.demo.issue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanle.demo.issue.dto.bo.IssueBO;
import com.quanle.demo.issue.dto.bo.IssuePageQueryBO;
import com.quanle.demo.issue.model.IssueModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 问题表 Mapper。只返回 Model 或 BO。
 *
 * @author quanle
 */
@Mapper
public interface IssueMapper extends BaseMapper<IssueModel> {

    /**
     * 按提交人分页查询问题。
     *
     * @param page 分页参数，拦截器会改写 SQL
     * @param query 筛选条件
     * @return 分页数据
     */
    Page<IssueBO> selectIssuePage(Page<IssueBO> page, @Param("query") IssuePageQueryBO query);

    /**
     * 按期望状态和提交人条件更新，返回影响行数。
     *
     * @param id 问题 ID
     * @param expectStatus 期望的当前状态
     * @param targetStatus 目标状态
     * @param reporterId 提交人
     * @return 影响行数
     */
    int updateIssueStatus(
            @Param("id") Long id,
            @Param("expectStatus") Integer expectStatus,
            @Param("targetStatus") Integer targetStatus,
            @Param("reporterId") Long reporterId);
}
