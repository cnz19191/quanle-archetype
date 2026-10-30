package com.isiniu.demo.auth.repository.impl;

import com.isiniu.common.mybatis.MpBaseServiceImpl;
import com.isiniu.demo.auth.mapper.UserMapper;
import com.isiniu.demo.auth.model.UserModel;
import com.isiniu.demo.auth.repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 用户数据访问实现。
 *
 * @author isiniu
 */
@Repository
public class UserRepositoryImpl extends MpBaseServiceImpl<UserMapper, UserModel> implements UserRepository {

    @Override
    public UserModel getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return lambdaQuery().eq(UserModel::getUsername, username).one();
    }

    @Override
    public UserModel getByExternalId(String externalId) {
        if (!StringUtils.hasText(externalId)) {
            return null;
        }
        return lambdaQuery().eq(UserModel::getExternalId, externalId).one();
    }
}
