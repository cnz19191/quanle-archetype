package com.quanle.common.web;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 记录 {@link OperationLog} 标记的写操作。
 *
 * @author quanle
 */
@Slf4j
@Aspect
public class OperationLogAspect {

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        String method = joinPoint.getSignature().toShortString();
        log.info("[操作日志] module={}, action={}, method={}", operationLog.module(), operationLog.action(), method);
        try {
            Object result = joinPoint.proceed();
            log.info(
                    "[操作日志完成] module={}, action={}, method={}, costMs={}",
                    operationLog.module(),
                    operationLog.action(),
                    method,
                    System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.warn(
                    "[操作日志失败] module={}, action={}, method={}, costMs={}",
                    operationLog.module(),
                    operationLog.action(),
                    method,
                    System.currentTimeMillis() - start);
            throw ex;
        }
    }
}
