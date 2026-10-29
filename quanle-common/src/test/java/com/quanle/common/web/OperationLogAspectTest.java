package com.quanle.common.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

/**
 * 操作日志切面只记日志，不改变返回值。
 *
 * @author quanle
 */
class OperationLogAspectTest {

    @Test
    void recordsModuleAndAction() {
        Logger logger = (Logger) LoggerFactory.getLogger(OperationLogAspect.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            AspectJProxyFactory factory = new AspectJProxyFactory(new Sample());
            factory.addAspect(new OperationLogAspect());
            Sample proxy = factory.getProxy();

            assertEquals("ok", proxy.create());
            assertTrue(appender.list.stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("module=问题管理")
                            && event.getFormattedMessage().contains("action=新建问题")));
        } finally {
            logger.detachAppender(appender);
        }
    }

    static class Sample {

        @OperationLog(module = "问题管理", action = "新建问题")
        public String create() {
            return "ok";
        }
    }
}
