package com.quanle.common.mybatis;

import com.baomidou.mybatisplus.spring.service.IService;

/**
 * Repository 继承的基础能力，封装 MyBatis Plus {@link IService}。
 *
 * @param <T> 实体
 * @author quanle
 */
public interface MpBaseService<T> extends IService<T> {}
