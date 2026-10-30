package com.isiniu.common.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.isiniu.common.security.LoginUser;
import com.isiniu.common.web.LoginUserHolder;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 认证结果写入 LoginUserHolder，请求结束后清理。
 *
 * @author isiniu
 */
class LoginUserContextFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        LoginUserHolder.clear();
    }

    @Test
    void copiesAuthenticatedUserIntoHolderAndClearsAfterRequest() throws Exception {
        LoginUser user = new LoginUser(7L, "alice", List.of("USER"));
        SecurityContextHolder.getContext()
                .setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, null, List.of()));
        AtomicReference<LoginUser> seen = new AtomicReference<>();

        new LoginUserContextFilter()
                .doFilter(
                        new MockHttpServletRequest(),
                        new MockHttpServletResponse(),
                        (request, response) -> seen.set(LoginUserHolder.current()));

        assertEquals(user, seen.get());
        assertNull(LoginUserHolder.current());
    }

    @Test
    void leavesHolderEmptyWhenAnonymous() throws Exception {
        AtomicReference<Long> seen = new AtomicReference<>(-1L);

        new LoginUserContextFilter()
                .doFilter(
                        new MockHttpServletRequest(),
                        new MockHttpServletResponse(),
                        (request, response) -> seen.set(LoginUserHolder.getUserId()));

        assertNull(seen.get());
    }
}
