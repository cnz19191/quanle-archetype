package com.isiniu.common.auth;

import com.isiniu.common.api.CommonResult;
import com.isiniu.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * 安全过滤链里的 401 / 403 响应，保持和 {@link CommonResult} 一致的格式。
 *
 * @author isiniu
 */
public class SecurityErrorWriter {

    private final JsonMapper jsonMapper;

    public SecurityErrorWriter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * 写出统一错误响应。
     *
     * @param response 响应
     * @param status HTTP 状态
     * @param errorCode 错误码
     * @param message 提示
     * @throws IOException 写响应失败
     */
    public void write(HttpServletResponse response, HttpStatus status, ErrorCode errorCode, String message)
            throws IOException {
        if (response.isCommitted()) {
            return;
        }
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getWriter(), CommonResult.buildErrorResult(errorCode, message));
    }
}
