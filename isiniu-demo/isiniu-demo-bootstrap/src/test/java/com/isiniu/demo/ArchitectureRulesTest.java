package com.isiniu.demo;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isiniu.demo.comment.archfixture.CommentUsesIssueInternals;
import com.isiniu.demo.comment.archfixture.CommentUsesIssuePublicApi;
import com.isiniu.demo.issue.archfixture.IssueUsesSpringSecurity;
import com.isiniu.demo.portal.archfixture.PortalUsesIssueService;
import com.isiniu.demo.portal.archfixture.PortalUsesIssueServiceImpl;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

/**
 * 架构规则的反例测试：用测试源码里的违规样例类证明规则真的会报错，正例证明白名单不会误报。
 *
 * @author isiniu
 */
class ArchitectureRulesTest {

    private static JavaClasses importClasses(Class<?>... classes) {
        return new ClassFileImporter().importClasses(classes);
    }

    @Test
    void crossDomainRuleRejectsIssueInternalsFromComment() {
        AssertionError error = assertThrows(AssertionError.class, () -> ArchitectureRules.crossDomainWhitelist()
                .check(importClasses(CommentUsesIssueInternals.class)));

        String message = error.getMessage();
        assertTrue(message.contains("IssueRepository"), message);
        assertTrue(message.contains("IssueModel"), message);
        assertTrue(message.contains("IssueService"), message);
    }

    @Test
    void crossDomainRuleRejectsServiceImplFromPortal() {
        AssertionError error = assertThrows(AssertionError.class, () -> ArchitectureRules.crossDomainWhitelist()
                .check(importClasses(PortalUsesIssueServiceImpl.class)));

        assertTrue(error.getMessage().contains("IssueServiceImpl"), error.getMessage());
    }

    @Test
    void crossDomainRuleAllowsWhitelistedDependencies() {
        assertDoesNotThrow(() -> ArchitectureRules.crossDomainWhitelist()
                .check(importClasses(CommentUsesIssuePublicApi.class, PortalUsesIssueService.class)));
    }

    @Test
    void springSecurityRuleRejectsBusinessUsage() {
        assertThrows(AssertionError.class, () -> ArchitectureRules.noSpringSecurityOutsideAuth()
                .check(importClasses(IssueUsesSpringSecurity.class)));
    }

    /** 记录原写法的问题：noClasses().should(自定义条件) 会把违规事件取反，同一个反例也不报错。 */
    @Test
    void noClassesWithCustomConditionNeverFails() {
        ArchCondition<JavaClass> alwaysViolated = new ArchCondition<>("总是违规") {
            @Override
            public void check(JavaClass clazz, ConditionEvents events) {
                events.add(SimpleConditionEvent.violated(clazz, clazz.getName() + " 违规"));
            }
        };

        assertDoesNotThrow(
                () -> noClasses().should(alwaysViolated).check(importClasses(CommentUsesIssueInternals.class)));
    }
}
