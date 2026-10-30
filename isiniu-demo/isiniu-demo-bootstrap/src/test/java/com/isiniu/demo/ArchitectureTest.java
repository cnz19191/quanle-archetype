package com.isiniu.demo;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 分层和跨领域依赖门禁，对应 AGENTS.md 第 13 节，并禁止领域之间引用内部类型。
 *
 * @author isiniu
 */
@AnalyzeClasses(
        packages = "com.isiniu.demo",
        importOptions = com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String BASE = "com.isiniu.demo";

    @ArchTest
    static final ArchRule controller_should_not_depend_on_repository_or_mapper = noClasses()
            .that()
            .resideInAPackage(BASE + "..controller..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(BASE + "..repository..", BASE + "..mapper..")
            .as("controller 不依赖 repository、mapper");

    @ArchTest
    static final ArchRule service_should_not_depend_on_mapper = noClasses()
            .that()
            .resideInAPackage(BASE + "..service..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage(BASE + "..mapper..")
            .as("service 不依赖 mapper");

    @ArchTest
    static final ArchRule mapper_should_not_depend_on_response = noClasses()
            .that()
            .resideInAPackage(BASE + "..mapper..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage(BASE + "..dto.response..")
            .as("mapper 不依赖 dto.response");

    @ArchTest
    static final ArchRule no_new_code_in_domain_package =
            noClasses().should().resideInAPackage(BASE + "..domain..").as("新代码不放 domain 包");

    @ArchTest
    static final ArchRule no_dependency_on_domain_package = noClasses()
            .that()
            .resideInAPackage(BASE + "..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage(BASE + "..domain..")
            .as("新代码不依赖 domain 包");

    @ArchTest
    static final ArchRule no_autowired = noFields()
            .that()
            .areDeclaredInClassesThat()
            .resideInAPackage(BASE + "..")
            .should()
            .beAnnotatedWith(Autowired.class)
            .as("注入使用 @Resource");

    @ArchTest
    static final ArchRule business_should_not_depend_on_spring_security = noClasses()
            .that()
            .resideInAPackage(BASE + "..")
            .and()
            .resideOutsideOfPackage(BASE + ".auth..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("org.springframework.security..")
            .as("业务代码只通过 LoginUserHolder 取用户，不直接依赖 Spring Security");

    @ArchTest
    static final ArchRule no_javax_ee = noClasses()
            .that()
            .resideInAPackage(BASE + "..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "javax.servlet..",
                    "javax.persistence..",
                    "javax.validation..",
                    "javax.inject..",
                    "javax.ws.rs..",
                    "javax.transaction..")
            .as("import 使用 jakarta");

    @ArchTest
    static final ArchRule domain_internals_stay_inside_domain = ArchRuleDefinition.noClasses()
            .should(new ArchCondition<JavaClass>("依赖其他领域的 repository、mapper、model 或实现") {
                @Override
                public void check(JavaClass clazz, ConditionEvents events) {
                    String owner = domainOf(clazz.getPackageName());
                    for (Dependency dependency : clazz.getDirectDependenciesFromSelf()) {
                        JavaClass target = dependency.getTargetClass();
                        String targetPackage = target.getPackageName();
                        if (!isDomainInternal(targetPackage)) {
                            continue;
                        }
                        String targetDomain = domainOf(targetPackage);
                        if (targetDomain != null && !targetDomain.equals(owner)) {
                            events.add(SimpleConditionEvent.violated(
                                    clazz, clazz.getName() + " 不应依赖 " + target.getName()));
                        }
                    }
                }
            });

    private static String domainOf(String packageName) {
        String prefix = BASE + ".";
        if (packageName == null || !packageName.startsWith(prefix)) {
            return null;
        }
        String rest = packageName.substring(prefix.length());
        int dot = rest.indexOf('.');
        if (dot < 0) {
            return rest;
        }
        return rest.substring(0, dot);
    }

    private static boolean isDomainInternal(String packageName) {
        if (packageName == null || !packageName.startsWith(BASE + ".")) {
            return false;
        }
        return packageName.contains(".repository")
                || packageName.contains(".mapper")
                || packageName.contains(".model")
                || packageName.contains(".convert")
                || packageName.contains(".service.impl")
                || packageName.contains(".dto.bo")
                || packageName.contains(".controller")
                || packageName.contains(".client");
    }
}
