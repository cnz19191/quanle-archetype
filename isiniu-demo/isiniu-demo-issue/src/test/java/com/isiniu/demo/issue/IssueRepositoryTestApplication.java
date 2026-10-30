package com.isiniu.demo.issue;

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
@ComponentScan(basePackages = "com.isiniu.demo.issue.repository")
@MapperScan(basePackages = "com.isiniu.demo.issue.mapper", annotationClass = Mapper.class)
public class IssueRepositoryTestApplication {}
