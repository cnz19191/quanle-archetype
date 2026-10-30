package com.isiniu.demo.auth.repository.impl;

import com.isiniu.common.mybatis.MpBaseServiceImpl;
import com.isiniu.demo.auth.mapper.RefreshTokenMapper;
import com.isiniu.demo.auth.model.RefreshTokenModel;
import com.isiniu.demo.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 刷新令牌数据访问实现。
 *
 * @author isiniu
 */
@Repository
public class RefreshTokenRepositoryImpl extends MpBaseServiceImpl<RefreshTokenMapper, RefreshTokenModel>
        implements RefreshTokenRepository {

    @Override
    public RefreshTokenModel getByTokenHash(String tokenHash) {
        if (!StringUtils.hasText(tokenHash)) {
            return null;
        }
        return lambdaQuery().eq(RefreshTokenModel::getTokenHash, tokenHash).one();
    }
}
