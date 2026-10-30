package com.isiniu.single.auth.repository;

import com.isiniu.common.mybatis.MpBaseService;
import com.isiniu.single.auth.model.UserModel;

/**
 * 用户数据访问。
 *
 * @author isiniu
 */
public interface UserRepository extends MpBaseService<UserModel> {

    /**
     * 按登录名查询未删除用户。
     *
     * @param username 登录名
     * @return 用户，不存在时返回 null
     */
    UserModel getByUsername(String username);

    /**
     * 按外部身份查询未删除用户。
     *
     * @param externalId 外部主体
     * @return 用户，不存在时返回 null
     */
    UserModel getByExternalId(String externalId);
}
