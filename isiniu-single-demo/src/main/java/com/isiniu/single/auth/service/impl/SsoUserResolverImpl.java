package com.isiniu.single.auth.service.impl;

import com.isiniu.common.auth.AuthProperties;
import com.isiniu.common.security.LoginUser;
import com.isiniu.common.security.SsoUserResolver;
import com.isiniu.single.auth.convert.AuthConvert;
import com.isiniu.single.auth.model.UserModel;
import com.isiniu.single.auth.repository.UserRepository;
import jakarta.annotation.Resource;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * SSO 用户映射：按 {@code sys_user.external_id} 绑定外部主体。
 *
 * @author isiniu
 */
@Slf4j
@Component
public class SsoUserResolverImpl implements SsoUserResolver {

    private static final int USERNAME_MAX_LENGTH = 64;

    @Resource
    private UserRepository userRepository;

    @Resource
    private AuthProperties authProperties;

    @Override
    public LoginUser resolve(String subject, Map<String, Object> claims) {
        return toEnabledLoginUser(userRepository.getByExternalId(subject));
    }

    @Override
    public LoginUser provision(String subject, Map<String, Object> claims) {
        if (!authProperties.getSso().isAutoProvision()) {
            return null;
        }
        UserModel existing = userRepository.getByExternalId(subject);
        if (existing != null) {
            return toEnabledLoginUser(existing);
        }
        UserModel user = new UserModel();
        user.setUsername(uniqueUsername(subject, claims));
        user.setDisplayName(claim(claims, "name", user.getUsername()));
        user.setExternalId(subject);
        user.setStatus(UserModel.STATUS_ENABLED);
        try {
            userRepository.save(user);
        } catch (DuplicateKeyException ex) {
            log.warn("[SSO 自动建号冲突] subject={}", subject, ex);
            UserModel concurrent = userRepository.getByExternalId(subject);
            return toEnabledLoginUser(concurrent);
        }
        log.info("[SSO 自动建号] userId={}, subject={}", user.getId(), subject);
        return AuthConvert.INSTANCE.toLoginUser(user);
    }

    /**
     * 只有启用状态的用户才能登录，停用用户一律返回 null。
     *
     * @param user 本地用户
     * @return 登录用户，用户不存在或已停用时返回 null
     */
    private LoginUser toEnabledLoginUser(UserModel user) {
        if (user == null || user.getStatus() == null || user.getStatus() != UserModel.STATUS_ENABLED) {
            return null;
        }
        return AuthConvert.INSTANCE.toLoginUser(user);
    }

    private String uniqueUsername(String subject, Map<String, Object> claims) {
        String preferred = claim(claims, "preferred_username", claim(claims, "email", "sso-" + subject));
        String username =
                preferred.length() > USERNAME_MAX_LENGTH ? preferred.substring(0, USERNAME_MAX_LENGTH) : preferred;
        return userRepository.getByUsername(username) == null ? username : username + "-" + shortHash(subject);
    }

    private String claim(Map<String, Object> claims, String key, String defaultValue) {
        Object value = claims.get(key);
        if (value instanceof String text && StringUtils.hasText(text)) {
            return text;
        }
        return defaultValue;
    }

    private String shortHash(String subject) {
        return Integer.toHexString(subject.hashCode());
    }
}
