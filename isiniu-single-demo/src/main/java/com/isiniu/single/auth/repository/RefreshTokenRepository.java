package com.isiniu.single.auth.repository;

import com.isiniu.common.mybatis.MpBaseService;
import com.isiniu.single.auth.model.RefreshTokenModel;

/**
 * 刷新令牌数据访问。
 *
 * @author isiniu
 */
public interface RefreshTokenRepository extends MpBaseService<RefreshTokenModel> {

    /**
     * 按哈希查询未删除的刷新令牌。
     *
     * @param tokenHash 令牌哈希
     * @return 令牌记录，不存在时返回 null
     */
    RefreshTokenModel getByTokenHash(String tokenHash);
}
