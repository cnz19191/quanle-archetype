package com.quanle.single.config;

/**
 * 业务错误码号段。各领域的 {@code ErrorCodeEnum} 使用项目前缀加本号段内的 4 位序号，全项目含义唯一。
 *
 * <p>0001-0999：问题领域 issue。新增领域时在这里登记下一段千号，并写进技术方案。
 *
 * @author quanle
 */
public final class ErrorCodeRange {

    public static final int ISSUE_MIN = 1;

    public static final int ISSUE_MAX = 999;

    private ErrorCodeRange() {}
}
