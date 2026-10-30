package com.isiniu.demo.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.isiniu.common.security.AuthProperties;
import com.isiniu.common.security.LoginUser;
import com.isiniu.demo.auth.model.UserModel;
import com.isiniu.demo.auth.repository.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * SSO 用户解析与自动建号。
 *
 * @author isiniu
 */
@ExtendWith(MockitoExtension.class)
class SsoUserResolverImplTest {

    private static final int STATUS_DISABLED = 0;

    @Mock
    private UserRepository userRepository;

    private final AuthProperties authProperties = new AuthProperties();

    private final SsoUserResolverImpl resolver = new SsoUserResolverImpl();

    @BeforeEach
    void setUp() {
        authProperties.getSso().setAutoProvision(true);
        ReflectionTestUtils.setField(resolver, "userRepository", userRepository);
        ReflectionTestUtils.setField(resolver, "authProperties", authProperties);
    }

    @Test
    void resolveReturnsEnabledUser() {
        when(userRepository.getByExternalId("sub")).thenReturn(user(UserModel.STATUS_ENABLED));

        LoginUser loginUser = resolver.resolve("sub", Map.of());

        assertNotNull(loginUser);
        assertEquals(5L, loginUser.userId());
    }

    @Test
    void resolveRejectsDisabledUser() {
        when(userRepository.getByExternalId("sub")).thenReturn(user(STATUS_DISABLED));

        assertNull(resolver.resolve("sub", Map.of()));
    }

    @Test
    void provisionRejectsExistingDisabledUser() {
        when(userRepository.getByExternalId("sub")).thenReturn(user(STATUS_DISABLED));

        assertNull(resolver.provision("sub", Map.of()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void provisionRejectsDisabledUserCreatedConcurrently() {
        when(userRepository.getByExternalId("sub")).thenReturn(null, user(STATUS_DISABLED));
        when(userRepository.save(any())).thenThrow(new DuplicateKeyException("duplicate"));

        assertNull(resolver.provision("sub", Map.of()));
    }

    @Test
    void provisionCreatesEnabledUser() {
        when(userRepository.getByExternalId("sub")).thenReturn(null);
        when(userRepository.save(any())).thenReturn(true);

        LoginUser loginUser = resolver.provision("sub", Map.of("preferred_username", "alice"));

        assertNotNull(loginUser);
        assertEquals("alice", loginUser.username());
    }

    @Test
    void provisionDisabledWhenAutoProvisionOff() {
        authProperties.getSso().setAutoProvision(false);

        assertNull(resolver.provision("sub", Map.of()));
        verify(userRepository, never()).getByExternalId(any());
    }

    private UserModel user(int status) {
        UserModel user = new UserModel();
        user.setId(5L);
        user.setUsername("tester");
        user.setExternalId("sub");
        user.setStatus(status);
        return user;
    }
}
