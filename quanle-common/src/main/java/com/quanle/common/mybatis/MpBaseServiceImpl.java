package com.quanle.common.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;

/**
 * Repository 实现的基类。业务代码通过 {@code baseMapper} 访问 Mapper，Service 不直接依赖 Mapper。
 *
 * @param <M> Mapper
 * @param <T> 实体
 * @author quanle
 */
public class MpBaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements MpBaseService<T> {}
