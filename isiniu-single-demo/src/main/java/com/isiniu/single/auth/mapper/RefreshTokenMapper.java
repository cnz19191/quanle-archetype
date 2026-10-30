package com.isiniu.single.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isiniu.single.auth.model.RefreshTokenModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 刷新令牌表 Mapper。
 *
 * @author isiniu
 */
@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshTokenModel> {}
