package com.isiniu.demo.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isiniu.demo.auth.model.RefreshTokenModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 刷新令牌表 Mapper。
 *
 * @author isiniu
 */
@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshTokenModel> {}
