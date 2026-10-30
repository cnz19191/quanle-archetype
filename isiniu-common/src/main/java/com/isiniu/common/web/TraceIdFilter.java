package com.isiniu.common.web;

import cn.hutool.core.util.IdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 从请求头读取或生成 traceId，写入 MDC，并在响应头回传。
 *
 * @author isiniu
 */
public class TraceIdFilter extends OncePerRequestFilter {

    private static final Pattern SAFE_TRACE_ID = Pattern.compile("^[A-Za-z0-9\\-]{8,64}$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = resolveTraceId(request.getHeader(TraceIdConstants.HEADER));
        MDC.put(TraceIdConstants.TRACE_ID, traceId);
        response.setHeader(TraceIdConstants.HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TraceIdConstants.TRACE_ID);
        }
    }

    private String resolveTraceId(String header) {
        if (StringUtils.hasText(header) && SAFE_TRACE_ID.matcher(header).matches()) {
            return header;
        }
        return IdUtil.fastSimpleUUID();
    }
}
