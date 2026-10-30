package com.isiniu.demo;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.List;
import java.util.Set;

/**
 * 需要反例测试的架构规则。{@link ArchitectureTest} 用它们检查生产代码，{@link ArchitectureRulesTest} 用违规样例类证明规则会报错。
 *
 * @author isiniu
 */
final class ArchitectureRules {

    static final String BASE = "com.isiniu.demo";

    /** 共享基础包，不属于任何业务领域。 */
    private static final Set<String> SHARED = Set.of("kernel", "web");

    /** 任何领域都可以引用其他领域 *-api 中的这些子包。 */
    private static final List<String> PUBLIC_SUB_PACKAGES =
            List.of("api", "dto.request", "dto.response", "enums", "exception");

    /** 门户是编排层，额外允许调用各领域的 service 接口（不含 service.impl）。 */
    private static final String PORTAL = "portal";

    private ArchitectureRules() {}

    /**
     * 跨领域依赖白名单。注意必须用 {@code classes().should(条件)}：条件里登记 violated 事件才会让规则失败。
     * 写成 {@code noClasses().should(条件)} 时 ArchUnit 会把事件取反，违规反而变成“满足”，规则永远不报错。
     */
    static ArchRule crossDomainWhitelist() {
        return classes()
                .that()
                .resideInAPackage(BASE + "..")
                .should(new ArchCondition<JavaClass>("只依赖其他领域公开的 api、dto.request、dto.response、enums、exception 包") {
                    @Override
                    public void check(JavaClass clazz, ConditionEvents events) {
                        String owner = domainOf(clazz.getPackageName());
                        if (owner == null) {
                            return;
                        }
                        for (Dependency dependency : clazz.getDirectDependenciesFromSelf()) {
                            JavaClass target = dependency.getTargetClass();
                            String targetDomain = domainOf(target.getPackageName());
                            if (targetDomain == null
                                    || SHARED.contains(targetDomain)
                                    || targetDomain.equals(owner)
                                    || isAllowed(owner, targetDomain, target.getPackageName())) {
                                continue;
                            }
                            events.add(SimpleConditionEvent.violated(
                                    dependency, clazz.getName() + " 不应依赖 " + target.getName()));
                        }
                    }
                })
                .allowEmptyShould(true)
                .as("跨领域依赖只允许白名单内的包");
    }

    /** 业务代码只通过 LoginUserHolder 取用户，不直接依赖 Spring Security。 */
    static ArchRule noSpringSecurityOutsideAuth() {
        return noClasses()
                .that()
                .resideInAPackage(BASE + "..")
                .and()
                .resideOutsideOfPackage(BASE + ".auth..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("org.springframework.security..")
                .as("业务代码只通过 LoginUserHolder 取用户，不直接依赖 Spring Security");
    }

    private static boolean isAllowed(String owner, String targetDomain, String targetPackage) {
        if (SHARED.contains(owner)) {
            return false;
        }
        String domainBase = BASE + "." + targetDomain + ".";
        for (String sub : PUBLIC_SUB_PACKAGES) {
            String allowed = domainBase + sub;
            if (targetPackage.equals(allowed) || targetPackage.startsWith(allowed + ".")) {
                return true;
            }
        }
        return PORTAL.equals(owner) && targetPackage.equals(domainBase + "service");
    }

    static String domainOf(String packageName) {
        String prefix = BASE + ".";
        if (packageName == null || !packageName.startsWith(prefix)) {
            return null;
        }
        String rest = packageName.substring(prefix.length());
        int dot = rest.indexOf('.');
        return dot < 0 ? rest : rest.substring(0, dot);
    }
}
