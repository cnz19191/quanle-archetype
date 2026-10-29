package com.quanle.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.quanle.common.api.CommonResult;
import com.quanle.common.enums.CommonErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * 全局异常转换为统一返回。
 *
 * @author quanle
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void customExceptionKeepsBusinessCode() {
        CustomException ex = new CustomException(CommonErrorCode.UNAUTHORIZED, "缺少请求头 X-User-Id");
        var response = handler.handleCustom(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        CommonResult<Void> body = response.getBody();
        assertEquals("SYS0003", body.getCode());
    }

    @Test
    void unknownExceptionDoesNotLeakMessage() {
        var response = handler.handleUnknown(new IllegalStateException("sql detail"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("SYS0002", response.getBody().getCode());
        assertEquals("系统异常", response.getBody().getMessage());
    }
}
