package com.isiniu.demo;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 分层和跨领域依赖门禁，对应 AGENTS.md 第 13 节，并以白名单约束跨领域依赖。规则定义在 {@link ArchitectureRules}，反例测试见 {@link ArchitectureRulesTest}。
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
    static final ArchRule business_should_not_depend_on_spring_security =
            ArchitectureRules.noSpringSecurityOutsideAuth();

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
    static final ArchRule cross_domain_dependencies_are_whitelisted = ArchitectureRules.crossDomainWhitelist();
}
