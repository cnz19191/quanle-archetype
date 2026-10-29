package com.quanle.common.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 写操作审计标记。切面只记日志，不落库。
 *
 * @author quanle
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 业务模块。
     *
     * @return 模块名
     */
    String module();

    /**
     * 动作。
     *
     * @return 动作名
     */
    String action();
}
