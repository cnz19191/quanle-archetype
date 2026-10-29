package com.quanle.common.web;

import com.quanle.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 记录接口出入口。不打印参数，避免把 token、手机号等敏感信息打进日志。
 *
 * @author quanle
 */
@Slf4j
@Aspect
public class ApiLogAspect {

    @Around(
            "within(@org.springframework.web.bind.annotation.RestController *) && execution(public * *(..)) && !within(org.springdoc..*)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.info("[接口入口] class={}, method={}", className, method);
        try {
            Object result = joinPoint.proceed();
            log.info("[接口出口] class={}, method={}, costMs={}", className, method, System.currentTimeMillis() - start);
            return result;
        } catch (CustomException ex) {
            log.warn(
                    "[接口业务异常] class={}, method={}, code={}, costMs={}",
                    className,
                    method,
                    ex.getErrorCode().getCode(),
                    System.currentTimeMillis() - start);
            throw ex;
        } catch (Throwable ex) {
            log.error(
                    "[接口异常] class={}, method={}, costMs={}", className, method, System.currentTimeMillis() - start, ex);
            throw ex;
        }
    }
}
