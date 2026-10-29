package com.quanle.common.api;

import com.quanle.common.exception.ErrorCode;
import com.quanle.common.web.TraceIdConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import org.slf4j.MDC;

/**
 * 统一返回体。{@code code=0} 表示成功，并带回当前 traceId。
 *
 * @param <T> 数据载荷
 * @author quanle
 */
@Data
@Schema(description = "统一返回")
public class CommonResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String SUCCESS_CODE = "0";

    @Schema(description = "业务码，0 表示成功")
    private String code;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "数据")
    private T data;

    @Schema(description = "链路追踪 ID")
    private String traceId;

    public static <T> CommonResult<T> buildSuccessResult() {
        return buildSuccessResult(null);
    }

    /**
     * 包装成功结果。
     *
     * @param data 数据
     * @param <T> 数据类型
     * @return 统一返回
     */
    public static <T> CommonResult<T> buildSuccessResult(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage("成功");
        result.setData(data);
        result.setTraceId(currentTraceId());
        return result;
    }

    /**
     * 包装失败结果。
     *
     * @param errorCode 错误码
     * @param message 提示
     * @param <T> 数据类型
     * @return 统一返回
     */
    public static <T> CommonResult<T> buildErrorResult(ErrorCode errorCode, String message) {
        CommonResult<T> result = new CommonResult<>();
        result.setCode(errorCode.getCode());
        result.setMessage(message);
        result.setTraceId(currentTraceId());
        return result;
    }

    private static String currentTraceId() {
        return MDC.get(TraceIdConstants.TRACE_ID);
    }
}
