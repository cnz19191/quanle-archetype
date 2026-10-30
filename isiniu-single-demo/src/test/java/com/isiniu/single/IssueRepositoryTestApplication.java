package com.isiniu.single;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 只加载问题领域的数据访问，避免拉起 Web。
 *
 * @author isiniu
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.isiniu.single.repository")
@MapperScan(basePackages = "com.isiniu.single.mapper", annotationClass = Mapper.class)
public class IssueRepositoryTestApplication {}
