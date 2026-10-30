package com.isiniu.common.security;

import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

/**
 * SSO 模式：把外部 JWT 映射成 {@link LoginUser}。
 *
 * @author isiniu
 */
public class SsoAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final SsoUserResolver userResolver;

    private final AuthProperties properties;

    public SsoAuthenticationConverter(SsoUserResolver userResolver, AuthProperties properties) {
        this.userResolver = userResolver;
        this.properties = properties;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        AuthProperties.Sso sso = properties.getSso();
        String subject = jwt.getClaimAsString(sso.getUserIdClaim());
        if (!StringUtils.hasText(subject)) {
            throw new BadCredentialsException("SSO token 缺少用户标识 claim: " + sso.getUserIdClaim());
        }
        LoginUser loginUser = userResolver.resolve(subject, jwt.getClaims());
        if (loginUser == null && sso.isAutoProvision()) {
            loginUser = userResolver.provision(subject, jwt.getClaims());
        }
        if (loginUser == null) {
            throw new BadCredentialsException("SSO 用户未绑定本地账号");
        }
        List<String> tokenRoles = jwt.getClaimAsStringList(sso.getRolesClaim());
        List<String> roles = loginUser.roles().isEmpty() && tokenRoles != null ? tokenRoles : loginUser.roles();
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        return UsernamePasswordAuthenticationToken.authenticated(loginUser, jwt, authorities);
    }
}
