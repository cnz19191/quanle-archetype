package com.quanle.demo.issue.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 状态枚举的查找和迁移规则。
 *
 * @author quanle
 */
class IssueStatusEnumTest {

    @Test
    void getDescAndGetByValue() {
        assertEquals("待处理", IssueStatusEnum.getDesc("PENDING"));
        assertEquals(IssueStatusEnum.PROCESSING, IssueStatusEnum.getByValue(1));
        assertNull(IssueStatusEnum.getDesc("NOPE"));
        assertNull(IssueStatusEnum.getByValue(9));
    }

    @Test
    void onlyAllowsForwardOneStep() {
        assertTrue(IssueStatusEnum.PENDING.canTransitTo(IssueStatusEnum.PROCESSING));
        assertFalse(IssueStatusEnum.PENDING.canTransitTo(IssueStatusEnum.CLOSED));
        assertTrue(IssueStatusEnum.PROCESSING.canTransitTo(IssueStatusEnum.CLOSED));
        assertFalse(IssueStatusEnum.CLOSED.canTransitTo(IssueStatusEnum.PENDING));
    }
}
