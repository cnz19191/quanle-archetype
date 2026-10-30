package com.isiniu.single.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.isiniu.single.auth.model.UserModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper。
 *
 * @author isiniu
 */
@Mapper
public interface UserMapper extends BaseMapper<UserModel> {}
