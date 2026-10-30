package com.isiniu.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.isiniu.common.enums.CommonErrorCode;
import com.isiniu.common.web.TraceIdConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * 统一返回体携带 traceId。
 *
 * @author isiniu
 */
class CommonResultTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void successResultUsesTraceIdFromMdc() {
        MDC.put(TraceIdConstants.TRACE_ID, "trace-1");
        CommonResult<String> result = CommonResult.buildSuccessResult("ok");
        assertEquals(CommonResult.SUCCESS_CODE, result.getCode());
        assertEquals("成功", result.getMessage());
        assertEquals("ok", result.getData());
        assertEquals("trace-1", result.getTraceId());
    }

    @Test
    void errorResultUsesErrorCode() {
        CommonResult<Void> result = CommonResult.buildErrorResult(CommonErrorCode.PARAM_INVALID, "标题不能为空");
        assertEquals("SYS0001", result.getCode());
        assertEquals("标题不能为空", result.getMessage());
        assertNull(result.getTraceId());
    }
}
