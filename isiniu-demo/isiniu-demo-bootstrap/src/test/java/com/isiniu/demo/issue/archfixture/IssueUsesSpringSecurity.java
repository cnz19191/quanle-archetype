package com.isiniu.demo.issue.archfixture;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * ArchUnit 反例：业务领域直接读取 Spring Security 上下文，规则必须报错。
 *
 * @author isiniu
 */
public class IssueUsesSpringSecurity {

    public Object principal() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
