package com.isiniu.common.exception;

/**
 * 错误码契约。业务错误码与框架错误码都实现本接口。
 *
 * @author isiniu
 */
public interface ErrorCode {

    /**
     * 错误码，格式为模块前缀加 4 位序号。
     *
     * @return 错误码
     */
    String getCode();

    /**
     * 错误码说明。
     *
     * @return 说明
     */
    String getMessage();
}
