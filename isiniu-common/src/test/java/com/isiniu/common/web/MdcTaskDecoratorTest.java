package com.isiniu.common.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * 异步线程传递 traceId。
 *
 * @author isiniu
 */
class MdcTaskDecoratorTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void copiesMdcIntoTaskAndRestoresCaller() {
        MDC.put(TraceIdConstants.TRACE_ID, "from-caller");
        MdcTaskDecorator decorator = new MdcTaskDecorator();
        AtomicReference<String> seen = new AtomicReference<>();
        Runnable task = decorator.decorate(() -> seen.set(MDC.get(TraceIdConstants.TRACE_ID)));
        MDC.put(TraceIdConstants.TRACE_ID, "changed");

        task.run();

        assertEquals("from-caller", seen.get());
        assertEquals("changed", MDC.get(TraceIdConstants.TRACE_ID));
    }
}
