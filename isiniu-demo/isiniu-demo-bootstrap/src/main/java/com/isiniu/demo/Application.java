package com.isiniu.demo;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 模块化单体启动入口。组件扫描覆盖 kernel、web、领域和 portal。
 *
 * @author isiniu
 */
@SpringBootApplication
@MapperScan(basePackages = "com.isiniu.demo", annotationClass = Mapper.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
