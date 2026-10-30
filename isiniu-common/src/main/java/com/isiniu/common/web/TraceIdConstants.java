package com.isiniu.common.web;

/**
 * 链路追踪常量。日志 pattern 使用 {@code %X{traceId}}。
 *
 * @author isiniu
 */
public final class TraceIdConstants {

    public static final String TRACE_ID = "traceId";

    public static final String HEADER = "X-Trace-Id";

    private TraceIdConstants() {}
}
