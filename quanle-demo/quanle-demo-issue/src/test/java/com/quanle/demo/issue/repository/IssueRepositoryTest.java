package com.quanle.demo.issue.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanle.demo.issue.IssueRepositoryTestApplication;
import com.quanle.demo.issue.dto.bo.IssueBO;
import com.quanle.demo.issue.dto.bo.IssuePageQueryBO;
import com.quanle.demo.issue.enums.IssueStatusEnum;
import com.quanle.demo.issue.model.IssueModel;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Mapper XML 与逻辑删除。使用 H2 的 MySQL 模式，不连接真实 MySQL。
 *
 * @author quanle
 */
@SpringBootTest(classes = IssueRepositoryTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class IssueRepositoryTest {

    @Resource
    private IssueRepository issueRepository;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        jdbcTemplate.update("DELETE FROM issue_main");
    }

    @Test
    void pageFiltersByReporterAndTitle() {
        save("打印机无法连接", 7L, "BIZ-1");
        save("另一件事", 8L, "BIZ-2");

        IssuePageQueryBO query = new IssuePageQueryBO();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setReporterId(7L);
        query.setTitle("打印");
        Page<IssueBO> page = issueRepository.pageIssue(query);

        assertEquals(1, page.getTotal());
        assertEquals("打印机无法连接", page.getRecords().get(0).getTitle());
        assertNotNull(page.getRecords().get(0).getCreateTime());
    }

    @Test
    void updateStatusOnlyMatchesExpectedStatus() {
        IssueModel model = save("打印机无法连接", 7L, "BIZ-3");
        int rows = issueRepository.updateStatus(
                model.getId(), IssueStatusEnum.PENDING.getValue(), IssueStatusEnum.PROCESSING.getValue(), 7L);
        assertEquals(1, rows);
        int again = issueRepository.updateStatus(
                model.getId(), IssueStatusEnum.PENDING.getValue(), IssueStatusEnum.CLOSED.getValue(), 7L);
        assertEquals(0, again);
        assertEquals(
                IssueStatusEnum.PROCESSING.getValue(),
                issueRepository.getById(model.getId()).getStatus());
    }

    @Test
    void logicDeleteKeepsRow() {
        IssueModel model = save("打印机无法连接", 7L, "BIZ-4");
        issueRepository.removeById(model.getId());
        assertNull(issueRepository.getById(model.getId()));
        Integer deleted = jdbcTemplate.queryForObject(
                "SELECT deleted FROM issue_main WHERE id = ?", Integer.class, model.getId());
        assertEquals(1, deleted);
    }

    @Test
    void bizNoIsUnique() {
        save("打印机无法连接", 7L, "BIZ-5");
        assertThrows(DuplicateKeyException.class, () -> save("另一标题", 8L, "BIZ-5"));
    }

    private IssueModel save(String title, Long reporterId, String bizNo) {
        IssueModel model = new IssueModel();
        model.setTitle(title);
        model.setContent("一楼");
        model.setStatus(IssueStatusEnum.PENDING.getValue());
        model.setReporterId(reporterId);
        model.setBizNo(bizNo);
        issueRepository.save(model);
        assertNotNull(model.getId());
        assertNotNull(model.getCreateTime());
        assertNotNull(model.getUpdateTime());
        return model;
    }
}
