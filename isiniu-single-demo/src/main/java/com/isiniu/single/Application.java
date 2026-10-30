package com.isiniu.single;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 单模块启动入口。
 *
 * <p>Boot 4 把默认用户自动配置挪到 {@code spring-boot-security}。这里按类名排除，和模块化标杆保持一致。
 *
 * @author isiniu
 */
@SpringBootApplication(
        excludeName = "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration")
@MapperScan(basePackages = "com.isiniu.single", annotationClass = Mapper.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
