package com.isiniu.demo;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 模块化单体启动入口。组件扫描覆盖 kernel、web、领域和 portal。
 *
 * <p>Boot 4 把默认用户自动配置挪到 {@code spring-boot-security}。启动模块只在运行期通过 auth 依赖它，所以按类名排除，避免编译期依赖 Spring Security。
 *
 * @author isiniu
 */
@SpringBootApplication(
        excludeName = "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration")
@MapperScan(basePackages = "com.isiniu.demo", annotationClass = Mapper.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
