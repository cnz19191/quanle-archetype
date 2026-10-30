package com.isiniu.demo.auth.convert;

import com.isiniu.common.security.LoginUser;
import com.isiniu.demo.auth.dto.response.LoginUserResponse;
import com.isiniu.demo.auth.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 鉴权领域对象转换。
 *
 * @author isiniu
 */
@Mapper
public interface AuthConvert {

    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    /**
     * 用户表实体转登录用户。
     *
     * @param model 用户表实体
     * @return 登录用户
     */
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "roles", expression = "java(java.util.List.of())")
    LoginUser toLoginUser(UserModel model);

    /**
     * 用户表实体转返回对象。
     *
     * @param model 用户表实体
     * @return 当前用户
     */
    LoginUserResponse toLoginUserResponse(UserModel model);
}
