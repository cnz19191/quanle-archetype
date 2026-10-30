package com.isiniu.common.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * traceId 过滤器。
 *
 * @author isiniu
 */
class TraceIdFilterTest {

    @Test
    void generatesTraceIdWhenHeaderMissing() throws Exception {
        TraceIdFilter filter = new TraceIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();
        FilterChain chain = (req, res) -> seen.set(MDC.get(TraceIdConstants.TRACE_ID));

        filter.doFilter(request, response, chain);

        assertNotNull(seen.get());
        assertEquals(seen.get(), response.getHeader(TraceIdConstants.HEADER));
        assertNull(MDC.get(TraceIdConstants.TRACE_ID));
    }

    @Test
    void keepsSafeIncomingTraceId() throws Exception {
        TraceIdFilter filter = new TraceIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdConstants.HEADER, "abc12345");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();
        FilterChain chain = (req, res) -> seen.set(MDC.get(TraceIdConstants.TRACE_ID));

        filter.doFilter(request, response, chain);

        assertEquals("abc12345", seen.get());
        assertEquals("abc12345", response.getHeader(TraceIdConstants.HEADER));
    }

    @Test
    void rejectsTraceIdWithLineBreak() throws Exception {
        TraceIdFilter filter = new TraceIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdConstants.HEADER, "bad\nid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();
        FilterChain chain = (req, res) -> seen.set(MDC.get(TraceIdConstants.TRACE_ID));

        filter.doFilter(request, response, chain);

        assertNotEquals("bad\nid", seen.get());
        assertNotNull(seen.get());
    }
}
