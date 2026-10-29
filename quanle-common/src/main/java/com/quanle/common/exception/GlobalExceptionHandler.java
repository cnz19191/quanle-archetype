package com.quanle.common.exception;

import com.quanle.common.api.CommonResult;
import com.quanle.common.enums.CommonErrorCode;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * 把异常统一转换成 {@link CommonResult}。Controller 内不再写 try-catch。
 *
 * @author quanle
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonResult<Void>> handleCustom(CustomException ex) {
        log.warn("[业务异常] code={}, message={}", ex.getErrorCode().getCode(), ex.getMessage());
        HttpStatus status = statusOf(ex.getErrorCode());
        return ResponseEntity.status(status).body(CommonResult.buildErrorResult(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<CommonResult<Void>> handleBind(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fieldMessage)
                .collect(Collectors.joining("; "));
        return badRequest(message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<CommonResult<Void>> handleMethodValidation(HandlerMethodValidationException ex) {
        String message = ex.getAllValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("; "));
        return badRequest(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResult<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("[请求体无法解析] message={}", ex.getMessage());
        return badRequest("请求体格式不正确");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResult<Void>> handleUnknown(Exception ex) {
        log.error("[系统异常]", ex);
        CommonResult<Void> body =
                CommonResult.buildErrorResult(CommonErrorCode.SYSTEM_ERROR, CommonErrorCode.SYSTEM_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ResponseEntity<CommonResult<Void>> badRequest(String message) {
        String text = StringUtils.hasText(message) ? message : CommonErrorCode.PARAM_INVALID.getMessage();
        log.warn("[参数校验失败] message={}", text);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResult.buildErrorResult(CommonErrorCode.PARAM_INVALID, text));
    }

    private String fieldMessage(FieldError error) {
        if (StringUtils.hasText(error.getDefaultMessage())) {
            return error.getDefaultMessage();
        }
        return error.getField() + "不合法";
    }

    private HttpStatus statusOf(ErrorCode errorCode) {
        if (CommonErrorCode.UNAUTHORIZED.getCode().equals(errorCode.getCode())) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (CommonErrorCode.PARAM_INVALID.getCode().equals(errorCode.getCode())) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.OK;
    }
}
