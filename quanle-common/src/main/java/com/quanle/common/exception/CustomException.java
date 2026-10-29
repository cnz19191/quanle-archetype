package com.quanle.common.exception;

import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * 业务异常。必须携带错误码，第三方失败时保留原始异常作为 cause。
 *
 * @author quanle
 */
@Getter
public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode, String detail) {
        super(buildMessage(errorCode, detail));
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, String detail, Throwable cause) {
        super(buildMessage(errorCode, detail), cause);
        this.errorCode = errorCode;
    }

    private static String buildMessage(ErrorCode errorCode, String detail) {
        if (errorCode == null) {
            return detail;
        }
        if (!StringUtils.hasText(detail)) {
            return errorCode.getMessage();
        }
        return errorCode.getMessage() + "：" + detail;
    }
}
